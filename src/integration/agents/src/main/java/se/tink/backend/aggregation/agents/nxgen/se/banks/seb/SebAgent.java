package se.tink.backend.aggregation.agents.nxgen.se.banks.seb;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class SebAgent extends SebBaseAgent {
    @Inject
    public SebAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider, new SebConfiguration());
    }
}
