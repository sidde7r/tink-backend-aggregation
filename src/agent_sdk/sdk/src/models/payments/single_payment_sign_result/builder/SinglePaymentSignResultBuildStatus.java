package se.tink.agent.sdk.models.payments.single_payment_sign_result.builder;

import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentStatus;

public interface SinglePaymentSignResultBuildStatus {
    SinglePaymentSignResultBuildDebtor status(PaymentStatus status);

    SinglePaymentSignResultBuildDebtor error(PaymentError error);
}
