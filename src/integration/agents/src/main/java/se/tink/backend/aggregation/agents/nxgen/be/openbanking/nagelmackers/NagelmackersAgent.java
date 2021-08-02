package se.tink.backend.aggregation.agents.nxgen.be.openbanking.nagelmackers;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.Xs2aDevelopersAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class NagelmackersAgent extends Xs2aDevelopersAgent {

    @Inject
    public NagelmackersAgent(AgentComponentProvider componentProvider) {
        super(componentProvider, "https://openbankingapi.nagelmackers.be");
    }
}
