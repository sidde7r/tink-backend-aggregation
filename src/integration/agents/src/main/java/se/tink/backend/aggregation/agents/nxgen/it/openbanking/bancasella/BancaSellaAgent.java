package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancasella;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class BancaSellaAgent extends FabricAgent {
    @Inject
    public BancaSellaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
