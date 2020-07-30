package se.tink.backend.aggregation.agents.nxgen.it.openbanking.credem;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class CredemAgent extends CbiGlobeAgent {

    @Inject
    public CredemAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }
}
