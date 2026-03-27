package com.bric.documents.dto;

import com.bric.documents.validation.ValidPaymentReceiptRequest;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

@ValidPaymentReceiptRequest
public record PaymentReceiptRequest(
    @NotNull @Positive Long accountId,
    Long paymentId,
    Long paymentMethodId,
    @Positive Long contactId
) {
    public PaymentReceiptRequest(Long accountId) {
        this(accountId, null, null, null);
    }
}
