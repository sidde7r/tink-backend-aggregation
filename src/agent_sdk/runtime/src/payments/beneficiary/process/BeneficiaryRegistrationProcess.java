package src.agent_sdk.runtime.src.payments.beneficiary.process;

import java.util.Optional;
import se.tink.agent.sdk.payments.beneficiary.generic.GenericBeneficiaryRegistrator;
import src.agent_sdk.runtime.src.instance.AgentInstance;

public interface BeneficiaryRegistrationProcess {
    Optional<GenericBeneficiaryRegistrator> tryInstantiate(AgentInstance agentInstance);
}
