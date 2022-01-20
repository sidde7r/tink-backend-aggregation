package se.tink.agent.sdk.models.payments.beneficiary_register_result;

import java.util.Optional;
import javax.annotation.Nullable;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.builder.BeneficiaryRegisterResultBuildError;
import se.tink.agent.sdk.storage.SerializableReference;

public class BeneficiaryRegisterResult {
    @Nullable private final SerializableReference bankReference;

    private final BeneficiaryState beneficiaryState;

    BeneficiaryRegisterResult(
            @Nullable SerializableReference bankReference, BeneficiaryState beneficiaryState) {
        this.bankReference = bankReference;
        this.beneficiaryState = beneficiaryState;
    }

    public Optional<SerializableReference> getBankReference() {
        return Optional.ofNullable(bankReference);
    }

    public BeneficiaryState getBeneficiaryState() {
        return beneficiaryState;
    }

    public static BeneficiaryRegisterResultBuildError builder() {
        return new BeneficiaryRegisterResultBuilder();
    }
}
