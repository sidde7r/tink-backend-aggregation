package se.tink.agent.sdk.models.payments.single_payment_register_result.builder;

public interface SinglePaymentRegisterResultBuildReference {
    SinglePaymentRegisterResultBuild bankReference(String reference);

    SinglePaymentRegisterResultBuild bankReference(Object reference);

    SinglePaymentRegisterResultBuild noBankReference();
}
