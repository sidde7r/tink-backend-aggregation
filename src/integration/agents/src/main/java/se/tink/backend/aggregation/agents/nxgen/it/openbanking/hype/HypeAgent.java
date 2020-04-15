package se.tink.backend.aggregation.agents.nxgen.it.openbanking.hype;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fabric.FabricAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class HypeAgent extends FabricAgent {
    @Inject
    public HypeAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
