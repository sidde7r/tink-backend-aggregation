package se.tink.agent.sdk.models.payments.bulk_payment_register_result.builder;

import se.tink.agent.sdk.models.payments.PaymentError;

public interface BulkPaymentRegisterResultBuildStatus {
    BulkPaymentRegisterResultBuild error(PaymentError error);

    BulkPaymentRegisterResultBuild noError();
}
