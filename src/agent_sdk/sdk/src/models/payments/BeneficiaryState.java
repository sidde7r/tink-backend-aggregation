package se.tink.agent.sdk.models.payments;

import java.util.Optional;
import javax.annotation.Nullable;

public class BeneficiaryState {
    @Nullable private final BeneficiaryError error;

    private BeneficiaryState(@Nullable BeneficiaryError error) {
        this.error = error;
    }

    public Optional<ConnectivityError> getError() {
        return Optional.ofNullable(error);
    }

    public static BeneficiaryState error(BeneficiaryError error) {
        return new BeneficiaryState(error);
    }

    public static BeneficiaryState successful() {
        return new BeneficiaryState(null);
    }
}
