package com.bric.documents.service;

import com.bric.core.domain.dto.DocumentMetadata;
import com.bric.core.domain.dto.GenerateDocumentMessage;
import com.bric.core.domain.entity.Document;
import com.bric.core.domain.enums.CorrespondenceType;
import com.bric.core.domain.enums.DocumentType;
import com.bric.documents.dto.PaymentReceiptRequest;
import io.quarkus.hibernate.reactive.panache.Panache;
import io.smallrye.mutiny.Uni;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.xml.bind.JAXBException;
import org.eclipse.microprofile.reactive.messaging.Channel;
import org.eclipse.microprofile.reactive.messaging.Emitter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApplicationScoped
public class DocumentsService {

    @Inject
    @Channel("document-request-out")
    Emitter<GenerateDocumentMessage> documentRequestEmitter;

    public Uni<Long> triggerPaymentReceipt(PaymentReceiptRequest request) {
        Map<String, String> data = new HashMap<>();
        data.put("accountId", String.valueOf(request.accountId()));
        if (request.paymentId() != null) {
            data.put("paymentId", String.valueOf(request.paymentId()));
        }
        if (request.paymentMethodId() != null) {
            data.put("paymentMethodId", String.valueOf(request.paymentMethodId()));
        }

        List<CorrespondenceType> correspondenceTypes = List.of(CorrespondenceType.EMAIL_WITH_ATTACHMENT);

        DocumentMetadata metadata = new DocumentMetadata(
                DocumentType.PAYMENT_RECEIPT,
                correspondenceTypes,
                data
        );

        // JAXB serialization is blocking — executed before the reactive chain
        // per Constitution Principle I (Reactive-First)
        String metadataXml;
        try {
            metadataXml = metadata.toXml();
        } catch (JAXBException e) {
            return Uni.createFrom().failure(
                    new RuntimeException("Failed to serialize document metadata", e));
        }

        Document document = new Document();
        document.type = DocumentType.PAYMENT_RECEIPT;
        document.correspondenceTypes = correspondenceTypes;
        document.metadata = metadataXml;
        document.status = com.bric.core.domain.enums.DataStatus.PENDING;
        document.prePersistAudit();

        // Use explicit transaction so message is published AFTER commit
        return Panache.withTransaction(() ->
                document.persist()
                        .map(persisted -> ((Document) persisted).id)
        ).invoke(docId -> {
            documentRequestEmitter.send(new GenerateDocumentMessage(docId));
        });
    }
}
