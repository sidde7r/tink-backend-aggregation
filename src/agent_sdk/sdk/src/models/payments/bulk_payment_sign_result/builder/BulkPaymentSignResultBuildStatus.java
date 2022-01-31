package se.tink.agent.sdk.models.payments.bulk_payment_sign_result.builder;

import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentStatus;

public interface BulkPaymentSignResultBuildStatus {
    BulkPaymentSignResultBuildDebtor status(PaymentStatus status);

    BulkPaymentSignResultBuildDebtor error(PaymentError error);
}
