package se.tink.backend.aggregation.agents.nxgen.be.openbanking.axa;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public final class AxaAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public AxaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api-dailybanking.axabank.be");
    }
}
