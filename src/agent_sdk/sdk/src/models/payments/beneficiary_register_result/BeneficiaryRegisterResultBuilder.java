package se.tink.agent.sdk.models.payments.beneficiary_register_result;

import se.tink.agent.sdk.models.payments.BeneficiaryError;
import se.tink.agent.sdk.models.payments.BeneficiaryState;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.builder.BeneficiaryRegisterResultBuild;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.builder.BeneficiaryRegisterResultBuildError;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.builder.BeneficiaryRegisterResultBuildReference;
import se.tink.agent.sdk.storage.SerializableReference;

public class BeneficiaryRegisterResultBuilder
        implements BeneficiaryRegisterResultBuildError,
                BeneficiaryRegisterResultBuildReference,
                BeneficiaryRegisterResultBuild {
    private SerializableReference bankReference;
    private BeneficiaryState beneficiaryState;

    BeneficiaryRegisterResultBuilder() {}

    @Override
    public BeneficiaryRegisterResultBuild error(BeneficiaryError error) {
        this.beneficiaryState = BeneficiaryState.error(error);
        return this;
    }

    @Override
    public BeneficiaryRegisterResultBuildReference noError() {
        this.beneficiaryState = BeneficiaryState.successful();
        return this;
    }

    @Override
    public BeneficiaryRegisterResultBuild bankReference(String reference) {
        this.bankReference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public BeneficiaryRegisterResultBuild bankReference(Object reference) {
        this.bankReference = SerializableReference.from(reference);
        return this;
    }

    @Override
    public BeneficiaryRegisterResultBuild noBankReference() {
        this.bankReference = null;
        return this;
    }

    @Override
    public BeneficiaryRegisterResult build() {
        return new BeneficiaryRegisterResult(this.bankReference, this.beneficiaryState);
    }
}
