package src.agent_sdk.runtime.src.payments.beneficiary.processes.generic;

import java.util.Optional;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;
import se.tink.agent.sdk.payments.features.beneficiary.RegisterBeneficiaryGeneric;
import src.agent_sdk.runtime.src.instance.AgentInstance;
import src.agent_sdk.runtime.src.payments.beneficiary.process.BeneficiaryRegistrationProcess;

public class GenericBeneficiaryRegistrationProcess implements BeneficiaryRegistrationProcess {
    @Override
    public Optional<GenericBeneficiaryRegistrator> tryInstantiate(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(RegisterBeneficiaryGeneric.class)
                .map(RegisterBeneficiaryGeneric::beneficiaryRegistrator);
    }
}
