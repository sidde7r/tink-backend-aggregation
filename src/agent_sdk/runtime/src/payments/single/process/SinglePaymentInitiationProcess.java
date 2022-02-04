package se.tink.agent.runtime.payments.single.process;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.payments.single.generic.GenericSinglePaymentInitiator;

public interface SinglePaymentInitiationProcess {
    Optional<GenericSinglePaymentInitiator> tryInstantiate(AgentInstance agentInstance);
}
