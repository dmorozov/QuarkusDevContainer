package com.bric.documents.dispatch.consumer;

import com.bric.core.domain.dto.GenerateDocumentMessage;
import com.bric.core.domain.entity.Document;
import com.bric.core.domain.enums.DataStatus;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import io.vertx.core.json.JsonObject;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.reactive.messaging.Incoming;
import org.jboss.logging.Logger;

@ApplicationScoped
public class DocumentSendRequestConsumer {

    private static final Logger LOG = Logger.getLogger(DocumentSendRequestConsumer.class);

    @Incoming("document-send-request-in")
    public Uni<Void> consume(JsonObject json) {
        GenerateDocumentMessage message = json.mapTo(GenerateDocumentMessage.class);
        LOG.infof("Received DOCUMENT_SEND_REQUEST for documentId=%d", message.getDocumentId());

        return Panache.withTransaction(() ->
                Document.<Document>findById(message.getDocumentId())
                        .onItem().transformToUni(document -> {
                            if (document == null) {
                                LOG.warnf("Document not found for documentId=%d", message.getDocumentId());
                                return Uni.createFrom().voidItem();
                            }

                            // Duplicate guard (FR-011): skip if already at or past DISPATCHED
                            if (document.status.ordinal() >= DataStatus.DISPATCHED.ordinal()) {
                                LOG.infof("Skipping already-dispatched document documentId=%d, status=%s",
                                        document.id, document.status);
                                return Uni.createFrom().voidItem();
                            }

                            // TODO: Implement sending correspondence
                            LOG.infof("TODO: Dispatch correspondence for documentId=%d, types=%s",
                                    document.id, document.correspondenceTypes);

                            document.status = DataStatus.DISPATCHED;
                            document.preUpdateAudit();

                            return document.persist().replaceWithVoid();
                        })
        ).onFailure().recoverWithUni(throwable -> {
            LOG.errorf(throwable, "Failed to process DOCUMENT_SEND_REQUEST for documentId=%d",
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
