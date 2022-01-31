package se.tink.agent.sdk.models.payments.single_payment_register_result;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.PaymentState;
import se.tink.agent.sdk.models.payments.single_payment_register_result.builder.SinglePaymentRegisterResultBuildError;
import se.tink.agent.sdk.storage.SerializableReference;

public class SinglePaymentRegisterResult {

    private final PaymentState paymentState;

    @Nullable private final SerializableReference bankReference;

    SinglePaymentRegisterResult(
            PaymentState paymentState, @Nullable SerializableReference bankReference) {
        this.paymentState = paymentState;
        this.bankReference = bankReference;
    }

    public PaymentState getPaymentState() {
        return paymentState;
    }

    public Optional<SerializableReference> getBankReference() {
        return Optional.ofNullable(bankReference);
    }

    public static SinglePaymentRegisterResultBuildError builder() {
        return new SinglePaymentRegisterResultBuilder();
    }
}
