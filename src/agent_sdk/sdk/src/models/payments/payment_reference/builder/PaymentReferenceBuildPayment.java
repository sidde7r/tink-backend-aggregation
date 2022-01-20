package se.tink.agent.sdk.models.payments.payment_reference.builder;

import se.tink.agent.sdk.models.payments.payment.Payment;

public interface PaymentReferenceBuildPayment {
    PaymentReferenceBuildBankReference payment(Payment payment);
}
