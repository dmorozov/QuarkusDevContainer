package com.bric.documents.generate.consumer;

import com.bric.core.domain.dto.GenerateDocumentMessage;
import com.bric.core.domain.entity.Document;
import com.bric.core.domain.enums.DataStatus;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DocumentRequestConsumer {

    private static final Logger LOG = Logger.getLogger(DocumentRequestConsumer.class);

    @Inject
    @Channel("document-send-request-out")
    Emitter<GenerateDocumentMessage> sendRequestEmitter;

    @Incoming("document-request-in")
    public Uni<Void> consume(JsonObject json) {
        GenerateDocumentMessage message = json.mapTo(GenerateDocumentMessage.class);
        LOG.infof("Received DOCUMENT_REQUEST for documentId=%d", message.getDocumentId());

        // Use explicit transaction so message publish happens AFTER commit
        return Panache.<Long>withTransaction(() ->
                Document.<Document>findById(message.getDocumentId())
                        .onItem().transformToUni(document -> {
                            if (document == null) {
                                LOG.warnf("Document not found for documentId=%d", message.getDocumentId());
                                return Uni.createFrom().nullItem();
                            }

                            // Duplicate guard (FR-011): skip if already at or past GENERATED
                            if (document.status.ordinal() >= DataStatus.GENERATED.ordinal()) {
                                LOG.infof("Skipping already-processed document documentId=%d, status=%s",
                                        document.id, document.status);
                                return Uni.createFrom().nullItem();
                            }

                            // TODO: Implement actual document generation logic
                            LOG.infof("TODO: Generate document for documentId=%d", document.id);

                            document.status = DataStatus.GENERATED;
                            document.preUpdateAudit();

                            return document.persist()
                                    .map(persisted -> ((Document) persisted).id);
                        })
        ).invoke(docId -> {
            // Published AFTER transaction commits
            if (docId != null) {
                sendRequestEmitter.send(new GenerateDocumentMessage(docId));
                LOG.infof("Published DOCUMENT_SEND_REQUEST for documentId=%d", docId);
            }
        }).replaceWithVoid()
        .onFailure().recoverWithUni(throwable -> {
            LOG.errorf(throwable, "Failed to process DOCUMENT_REQUEST for documentId=%d",
                    message.getDocumentId());
            // FR-007: Set status to FAILED on any exception
            return Panache.withTransaction(() ->
                    Document.<Document>findById(message.getDocumentId())
                            .onItem().transformToUni(doc -> {
                                if (doc != null) {
                                    doc.status = DataStatus.FAILED;
                                    doc.preUpdateAudit();
                                    return doc.persist().replaceWithVoid();
                                }
                                return Uni.createFrom().voidItem();
                            })
            );
        });
    }
}
