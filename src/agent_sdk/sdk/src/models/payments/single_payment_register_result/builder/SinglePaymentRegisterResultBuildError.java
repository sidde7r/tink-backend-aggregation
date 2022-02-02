package se.tink.agent.sdk.models.payments.single_payment_register_result.builder;

import se.tink.agent.sdk.models.payments.PaymentError;

public interface SinglePaymentRegisterResultBuildError {
    SinglePaymentRegisterResultBuild error(PaymentError error);

    SinglePaymentRegisterResultBuildReference noError();
}
