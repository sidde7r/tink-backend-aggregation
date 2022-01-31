package se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder;

import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

public interface BulkPaymentSignResultBuildReference {
    BulkPaymentSignResultBuildStatus reference(PaymentReference paymentReference);
}
