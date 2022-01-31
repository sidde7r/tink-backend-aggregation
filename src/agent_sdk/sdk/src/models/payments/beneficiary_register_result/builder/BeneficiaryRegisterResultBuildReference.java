package se.tink.agent.sdk.models.payments.beneficiary_register_result.builder;

public interface BeneficiaryRegisterResultBuildReference {
    BeneficiaryRegisterResultBuild bankReference(String reference);

    BeneficiaryRegisterResultBuild bankReference(Object reference);

    BeneficiaryRegisterResultBuild noBankReference();
}
