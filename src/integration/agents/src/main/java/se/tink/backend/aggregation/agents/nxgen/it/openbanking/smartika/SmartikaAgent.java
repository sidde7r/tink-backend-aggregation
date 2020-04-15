package se.tink.backend.aggregation.agents.nxgen.it.openbanking.smartika;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class SmartikaAgent extends FabricAgent {
    @Inject
    public SmartikaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
