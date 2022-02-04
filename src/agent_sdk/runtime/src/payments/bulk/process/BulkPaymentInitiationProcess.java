package se.tink.agent.runtime.payments.bulk.process;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;

public interface BulkPaymentInitiationProcess {
    Optional<GenericBulkPaymentInitiator> tryInstantiate(AgentInstance agentInstance);
}
