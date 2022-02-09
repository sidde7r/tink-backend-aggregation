package src.agent_sdk.compatibility_layers.aggregation_service.src.payments.report;

import java.util.Optional;
import javax.annotation.Nullable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import se.tink.agent.sdk.models.payments.ConnectivityError;
import se.tink.agent.sdk.models.payments.PaymentStatus;
import se.tink.agent.sdk.models.payments.payment.Debtor;
import se.tink.agent.sdk.models.payments.payment.Payment;
import se.tink.agent.sdk.models.payments.payment_reference.PaymentReference;

@Builder
@EqualsAndHashCode
public class PaymentInitiationState {
    private final PaymentReference paymentReference;

    @Nullable private final PaymentStatus status;

    @Nullable private final ConnectivityError error;

    @Nullable private final Debtor debtor;

    public Payment getPayment() {
        return paymentReference.getPayment();
    }

    public PaymentReference getPaymentReference() {
        return paymentReference;
    }

    public Optional<PaymentStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<ConnectivityError> getError() {
        return Optional.ofNullable(error);
    }

    public Optional<Debtor> getDebtor() {
        return Optional.ofNullable(debtor);
    }
}
