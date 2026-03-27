package com.bric.documents.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = PaymentReceiptRequestValidator.class)
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidPaymentReceiptRequest {
    String message() default "Either paymentId or paymentMethodId must be provided";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
