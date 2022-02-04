package src.agent_sdk.runtime.src.payments.single.processes.generic;

import java.util.Optional;
import se.tink.agent.sdk.payments.features.single.InitiateSinglePaymentGeneric;
import se.tink.agent.sdk.payments.single.generic.GenericSinglePaymentInitiator;
import src.agent_sdk.runtime.src.instance.AgentInstance;
import src.agent_sdk.runtime.src.payments.single.process.SinglePaymentInitiationProcess;

public class GenericSinglePaymentInitiationProcess implements SinglePaymentInitiationProcess {
    @Override
    public Optional<GenericSinglePaymentInitiator> tryInstantiate(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(InitiateSinglePaymentGeneric.class)
                .map(InitiateSinglePaymentGeneric::singlePaymentInitiator);
    }
}
