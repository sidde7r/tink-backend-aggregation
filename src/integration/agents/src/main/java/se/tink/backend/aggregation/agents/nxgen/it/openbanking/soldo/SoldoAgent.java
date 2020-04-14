package se.tink.backend.aggregation.agents.nxgen.it.openbanking.soldo;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class SoldoAgent extends FabricAgent {
    @Inject
    public SoldoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
