package se.tink.backend.aggregation.agents.nxgen.it.openbanking.borsadelcredito;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class BorsaDelCreditoAgent extends FabricAgent {
    @Inject
    public BorsaDelCreditoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
