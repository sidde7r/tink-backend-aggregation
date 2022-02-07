package se.tink.agent.sdk.models.payments.beneficiary_register_result;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.BeneficiaryError;
import se.tink.agent.sdk.models.payments.ConnectivityError;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.builder.BeneficiaryRegisterResultBuildError;
import se.tink.agent.sdk.storage.SerializableReference;

public class BeneficiaryRegisterResult {
    @Nullable private final SerializableReference bankReference;

    @Nullable private final BeneficiaryError error;

    BeneficiaryRegisterResult(
            @Nullable SerializableReference bankReference, @Nullable BeneficiaryError error) {
        this.bankReference = bankReference;
        this.error = error;
    }

    public Optional<SerializableReference> getBankReference() {
        return Optional.ofNullable(bankReference);
    }

    public Optional<ConnectivityError> getError() {
        return Optional.ofNullable(error);
    }

    public static BeneficiaryRegisterResultBuildError builder() {
        return new BeneficiaryRegisterResultBuilder();
    }
}
