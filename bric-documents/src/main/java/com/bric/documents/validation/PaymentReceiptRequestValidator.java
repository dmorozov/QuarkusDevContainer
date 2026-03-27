package com.bric.documents.validation;

import com.bric.documents.dto.PaymentReceiptRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PaymentReceiptRequestValidator
        implements ConstraintValidator<ValidPaymentReceiptRequest, PaymentReceiptRequest> {

    @Override
    public boolean isValid(PaymentReceiptRequest request, ConstraintValidatorContext context) {
        if (request == null) {
            return true;
        }
        return request.paymentId() != null || request.paymentMethodId() != null;
    }
}
