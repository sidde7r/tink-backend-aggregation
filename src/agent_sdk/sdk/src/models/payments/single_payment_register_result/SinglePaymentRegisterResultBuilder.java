package se.tink.agent.sdk.models.payments.single_payment_register_result;

import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.single_payment_register_result.builder.SinglePaymentRegisterResultBuild;
import se.tink.agent.sdk.models.payments.single_payment_register_result.builder.SinglePaymentRegisterResultBuildError;
import se.tink.agent.sdk.models.payments.single_payment_register_result.builder.SinglePaymentRegisterResultBuildReference;
import se.tink.agent.sdk.storage.SerializableReference;

public class SinglePaymentRegisterResultBuilder
        implements SinglePaymentRegisterResultBuildError,
                SinglePaymentRegisterResultBuildReference,
                SinglePaymentRegisterResultBuild {

    private SerializableReference bankReference;
    private PaymentState paymentState;

    SinglePaymentRegisterResultBuilder() {}

    @Override
    public SinglePaymentRegisterResultBuild error(PaymentError error) {
        this.paymentState = PaymentState.create(error);
        return this;
    }

    @Override
    public SinglePaymentRegisterResultBuildReference noError() {
        this.paymentState = PaymentState.create(PaymentStatus.CREATED);
        return this;
    }

    @Override
    public SinglePaymentRegisterResultBuild bankReference(String reference) {
        this.bankReference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public SinglePaymentRegisterResultBuild bankReference(Object reference) {
        this.bankReference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public SinglePaymentRegisterResultBuild noBankReference() {
        this.bankReference = null;
        return this;
    }

    @Override
    public SinglePaymentRegisterResult build() {
        return new SinglePaymentRegisterResult(this.paymentState, this.bankReference);
    }
}
