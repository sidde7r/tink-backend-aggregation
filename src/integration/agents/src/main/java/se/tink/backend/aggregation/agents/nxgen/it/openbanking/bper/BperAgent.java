package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bper;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class BperAgent extends CbiGlobeAgent {

    @Inject
    public BperAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }
}
