package se.tink.backend.aggregation.agents.nxgen.it.openbanking.findomestic;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class FindomesticAgent extends CbiGlobeAgent {

    @Inject
    public FindomesticAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }
}
