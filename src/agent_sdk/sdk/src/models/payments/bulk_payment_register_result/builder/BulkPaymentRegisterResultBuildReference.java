package se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder;

import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public interface BulkPaymentRegisterResultBuildReference {
    BulkPaymentRegisterResultBuildStatus reference(PaymentReference paymentReference);
}
