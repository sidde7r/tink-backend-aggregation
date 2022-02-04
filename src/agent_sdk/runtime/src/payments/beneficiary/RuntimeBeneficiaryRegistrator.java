package src.agent_sdk.runtime.src.payments.beneficiary;

import se.tink.agent.sdk.models.payments.beneficiary.Beneficiary;
import se.tink.agent.sdk.models.payments.beneficiary_register_result.BeneficiaryRegisterResult;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;
import se.tink.agent.sdk.payments.beneficiary.steppable_execution.BeneficiarySignFlow;
import se.tink.libraries.account.AccountIdentifier;

public class RuntimeBeneficiaryRegistrator {
    private final GenericBeneficiaryRegistrator agentBeneficiaryRegistrator;

    public RuntimeBeneficiaryRegistrator(
            GenericBeneficiaryRegistrator agentBeneficiaryRegistrator) {
        this.agentBeneficiaryRegistrator = agentBeneficiaryRegistrator;
    }

    public BeneficiaryRegisterResult registerBeneficiary(
            AccountIdentifier accountIdentifier, Beneficiary beneficiary) {
        return this.agentBeneficiaryRegistrator.registerBeneficiary(accountIdentifier, beneficiary);
    }

    public BeneficiarySignFlow getSignFlow() {
        return this.agentBeneficiaryRegistrator.getSignFlow();
    }
}
