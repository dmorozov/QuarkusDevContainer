package com.bric.documents.rest;

import com.bric.documents.dto.PaymentReceiptRequest;
import com.bric.documents.service.DocumentsService;
import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.validation.Valid;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;

@Path("/documents")
@Produces(MediaType.APPLICATION_JSON)
@Consumes(MediaType.APPLICATION_JSON)
public class DocumentsResource {

    @Inject
    DocumentsService documentsService;

    @POST
    @Path("/payment-receipt")
    public Uni<Response> paymentReceipt(@Valid PaymentReceiptRequest request) {
        return documentsService.triggerPaymentReceipt(request)
                .map(documentId -> Response.accepted()
                        .entity(java.util.Map.of("documentId", documentId))
                        .build());
    }
}
