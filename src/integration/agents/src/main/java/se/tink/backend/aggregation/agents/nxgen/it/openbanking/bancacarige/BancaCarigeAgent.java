package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancacarige;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bancacarige.client.BancaCarigeRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class BancaCarigeAgent extends CbiGlobeAgent {

    @Inject
    public BancaCarigeAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected BancaCarigeRequestBuilder buildRequestBuilder() {
        return new BancaCarigeRequestBuilder(
                client,
                randomValueGenerator,
                localDateTimeSource,
                providerConfiguration,
                strongAuthenticationState,
                getAgentConfiguration().getRedirectUrl(),
                psuIpAddress);
    }

    @Override
    protected String getBaseUrl() {
        return "https://openbanking.carige.it";
    }
}
