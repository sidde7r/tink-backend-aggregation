package src.agent_sdk.runtime.src.payments.single.process;

import java.util.Optional;
import se.tink.agent.sdk.payments.single.generic.GenericSinglePaymentInitiator;
import src.agent_sdk.runtime.src.instance.AgentInstance;

public interface SinglePaymentInitiationProcess {
    Optional<GenericSinglePaymentInitiator> tryInstantiate(AgentInstance agentInstance);
}
