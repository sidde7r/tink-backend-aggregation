package src.agent_sdk.runtime.src.payments.bulk.processes.generic;

import java.util.Optional;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;
import src.agent_sdk.runtime.src.instance.AgentInstance;
import src.agent_sdk.runtime.src.payments.bulk.process.BulkPaymentInitiationProcess;

public class GenericBulkPaymentInitiationProcess implements BulkPaymentInitiationProcess {
    @Override
    public Optional<GenericBulkPaymentInitiator> tryInstantiate(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(InitiateBulkPaymentGeneric.class)
                .map(InitiateBulkPaymentGeneric::bulkPaymentInitiator);
    }
}
