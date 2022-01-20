package se.tink.agent.sdk.models.payments.single_payment_sign_result;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.single_payment_sign_result.builder.SinglePaymentSignResultBuildStatus;

public class SinglePaymentSignResult {
    private final PaymentState paymentState;

    @Nullable private final Debtor paymentDebtor;

    SinglePaymentSignResult(PaymentState paymentState, @Nullable Debtor paymentDebtor) {
        this.paymentState = paymentState;
        this.paymentDebtor = paymentDebtor;
    }

    public PaymentState getPaymentState() {
        return paymentState;
    }

    public Optional<Debtor> getPaymentDebtor() {
        return Optional.ofNullable(paymentDebtor);
    }

    public static SinglePaymentSignResultBuildStatus builder() {
        return new SinglePaymentSignResultBuilder();
    }
}
