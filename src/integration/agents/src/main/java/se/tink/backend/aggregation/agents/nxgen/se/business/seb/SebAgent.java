package se.tink.backend.aggregation.agents.nxgen.se.business.seb;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.seb.SebBaseAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class SebAgent extends SebBaseAgent {
    @Inject
    public SebAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, new SebConfiguration());
    }
}
