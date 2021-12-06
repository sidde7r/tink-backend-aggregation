package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersTransactionalAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class CrelanAgent extends Xs2aDevelopersTransactionalAgent {

    @Inject
    public CrelanAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://api.crelan.be");
    }
}
