package se.tink.agent.sdk.models.payments.single_payment_sign_result.builder;

import se.tink.agent.sdk.models.payments.payment.Debtor;

public interface SinglePaymentSignResultBuildDebtor {
    SinglePaymentSignResultBuild debtor(Debtor debtor);

    SinglePaymentSignResultBuild noDebtor();
}
