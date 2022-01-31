package se.tink.agent.sdk.models.payments.single_payment_sign_result;

import com.google.common.base.Preconditions;
import se.tink.agent.sdk.models.payments.PaymentError;
import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.builder.SinglePaymentSignResultBuild;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.builder.SinglePaymentSignResultBuildDebtor;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.builder.SinglePaymentSignResultBuildStatus;

public class SinglePaymentSignResultBuilder
        implements SinglePaymentSignResultBuildStatus,
                SinglePaymentSignResultBuildDebtor,
                SinglePaymentSignResultBuild {
    private PaymentState paymentState;
    private Debtor debtor;

    SinglePaymentSignResultBuilder() {}

    @Override
    public SinglePaymentSignResultBuildDebtor status(PaymentStatus status) {
        this.paymentState = PaymentState.create(status);
        return this;
    }

    @Override
    public SinglePaymentSignResultBuildDebtor error(PaymentError error) {
        this.paymentState = PaymentState.create(error);
        return this;
    }

    @Override
    public SinglePaymentSignResultBuild debtor(Debtor debtor) {
        this.debtor = Preconditions.checkNotNull(debtor);
        return this;
    }

    @Override
    public SinglePaymentSignResultBuild noDebtor() {
        this.debtor = null;
        return this;
    }

    @Override
    public SinglePaymentSignResult build() {
        return new SinglePaymentSignResult(this.paymentState, this.debtor);
    }
}
