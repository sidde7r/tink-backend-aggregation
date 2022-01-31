package se.tink.agent.sdk.models.payments;

import com.google.common.base.Preconditions;
import java.util.Objects;
import java.util.Optional;
import javax.annotation.Nullable;

public class PaymentState {
    @Nullable private final PaymentStatus status;
    @Nullable private final PaymentError error;

    private PaymentState(@Nullable PaymentStatus status, @Nullable PaymentError error) {
        Preconditions.checkState(Objects.nonNull(status) || Objects.nonNull(error));
        this.status = status;
        this.error = error;
    }

    public Optional<PaymentStatus> getStatus() {
        return Optional.ofNullable(status);
    }

    public Optional<ConnectivityError> getError() {
        return Optional.ofNullable(error);
    }

    public static PaymentState create(PaymentStatus status) {
        return new PaymentState(status, null);
    }

    public static PaymentState create(PaymentError error) {
        return new PaymentState(null, error);
    }
}
