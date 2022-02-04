package se.tink.agent.runtime.payments.bulk.processes.generic;

import java.util.Optional;
import se.tink.agent.runtime.instance.AgentInstance;
import se.tink.agent.runtime.payments.bulk.process.BulkPaymentInitiationProcess;
import se.tink.agent.sdk.payments.bulk.generic.GenericBulkPaymentInitiator;
import se.tink.agent.sdk.payments.features.bulk.InitiateBulkPaymentGeneric;

public class GenericBulkPaymentInitiationProcess implements BulkPaymentInitiationProcess {
    @Override
    public Optional<GenericBulkPaymentInitiator> tryInstantiate(AgentInstance agentInstance) {
        return agentInstance
                .instanceOf(InitiateBulkPaymentGeneric.class)
                .map(InitiateBulkPaymentGeneric::bulkPaymentInitiator);
    }
}
