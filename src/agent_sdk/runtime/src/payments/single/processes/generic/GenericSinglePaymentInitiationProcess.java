package se.tink.agent.runtime.payments.single.processes.generic;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.runtime.payments.single.process.SinglePaymentInitiationProcess;
import se.tink.agent.sdk.payments.features.single.InitiateSinglePaymentGeneric;
import se.tink.agent.sdk.payments.single.generic.GenericSinglePaymentInitiator;

public class GenericSinglePaymentInitiationProcess implements SinglePaymentInitiationProcess {
    @Override
    public Optional<GenericSinglePaymentInitiator> tryInstantiate(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(InitiateSinglePaymentGeneric.class)
                .map(InitiateSinglePaymentGeneric::singlePaymentInitiator);
    }
}
