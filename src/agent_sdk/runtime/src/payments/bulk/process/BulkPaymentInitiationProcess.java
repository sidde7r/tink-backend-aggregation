package src.agent_sdk.runtime.src.payments.bulk.process;

import java.util.Optional;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import src.agent_sdk.runtime.src.instance.AgentInstance;

public interface BulkPaymentInitiationProcess {
    Optional<GenericBulkPaymentInitiator> tryInstantiate(AgentInstance agentInstance);
}
