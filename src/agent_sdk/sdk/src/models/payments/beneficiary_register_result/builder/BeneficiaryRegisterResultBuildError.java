package se.tink.agent.sdk.models.payments.beneficiary_register_result.builder;

import se.tink.agent.sdk.models.payments.BeneficiaryError;

public interface BeneficiaryRegisterResultBuildError {
    BeneficiaryRegisterResultBuild error(BeneficiaryError error);

    BeneficiaryRegisterResultBuildReference noError();
}
