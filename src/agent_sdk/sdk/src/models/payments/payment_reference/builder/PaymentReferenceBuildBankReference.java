package se.tink.agent.sdk.models.payments.payment_reference.builder;

public interface PaymentReferenceBuildBankReference {
    PaymentReferenceBuild noBankReference();

    PaymentReferenceBuild bankReference(String reference);

    PaymentReferenceBuild bankReference(Object reference);
}
