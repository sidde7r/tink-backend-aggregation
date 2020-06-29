package se.tink.backend.aggregation.agents.nxgen.se.business.swedbank;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public class SwedbankSEBusinessAgent extends SwedbankSEAgent {

    @Inject
    public SwedbankSEBusinessAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }
}
