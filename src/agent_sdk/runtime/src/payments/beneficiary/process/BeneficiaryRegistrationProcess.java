package se.tink.agent.runtime.payments.beneficiary.process;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;

public interface BeneficiaryRegistrationProcess {
    Optional<GenericBeneficiaryRegistrator> tryInstantiate(AgentInstance agentInstance);
}
