package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public final class CrelanAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public CrelanAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api.crelan.be");
    }
}
