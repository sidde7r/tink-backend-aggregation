package se.tink.agent.agents.example.payments;

import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.BeneficiaryRegisterResult;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;
import se.tink.agent.sdk.payments.beneficiary.steppable_execution.BeneficiarySignFlow;
import se.tink.libraries.account.AccountIdentifier;

public class ExampleBeneficiaryRegistrator implements GenericBeneficiaryRegistrator {
    @Override
    public BeneficiaryRegisterResult registerBeneficiary(
            AccountIdentifier debtorAccountIdentifier, Beneficiary beneficiary) {
        return null;
    }

    @Override
    public BeneficiarySignFlow getSignFlow() {
        return null;
    }
}
