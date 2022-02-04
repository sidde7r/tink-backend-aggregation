package se.tink.agent.runtime.payments.beneficiary.processes.generic;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.runtime.payments.beneficiary.process.BeneficiaryRegistrationProcess;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;
import se.tink.agent.sdk.payments.features.beneficiary.RegisterBeneficiaryGeneric;

public class GenericBeneficiaryRegistrationProcess implements BeneficiaryRegistrationProcess {
    @Override
    public Optional<GenericBeneficiaryRegistrator> tryInstantiate(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(RegisterBeneficiaryGeneric.class)
                .map(RegisterBeneficiaryGeneric::beneficiaryRegistrator);
    }
}
