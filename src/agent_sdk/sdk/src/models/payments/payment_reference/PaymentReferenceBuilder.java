package se.tink.agent.sdk.models.payments.payment_reference;

import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.builder.PaymentReferenceBuild;
import se.tink.agent.sdk.models.payments.payment_reference.builder.PaymentReferenceBuildBankReference;
import se.tink.agent.sdk.models.payments.payment_reference.builder.PaymentReferenceBuildPayment;
import se.tink.agent.sdk.storage.SerializableReference;

public class PaymentReferenceBuilder
        implements PaymentReferenceBuildPayment,
                PaymentReferenceBuildBankReference,
                PaymentReferenceBuild {

    private Payment payment;
    private SerializableReference bankReference;

    PaymentReferenceBuilder() {}

    @Override
    public PaymentReferenceBuildBankReference payment(Payment payment) {
        this.payment = payment;
        return this;
    }

    @Override
    public PaymentReferenceBuild noBankReference() {
        this.bankReference = null;
        return this;
    }

    @Override
    public PaymentReferenceBuild bankReference(String reference) {
        this.bankReference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public PaymentReferenceBuild bankReference(Object reference) {
        this.bankReference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public PaymentReference build() {
        return new PaymentReference(this.payment, this.bankReference);
    }
}
