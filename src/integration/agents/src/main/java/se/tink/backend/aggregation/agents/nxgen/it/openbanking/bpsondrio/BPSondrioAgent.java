package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpsondrio;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpsondrio.client.BPSondrioRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeRequestBuilder;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class BPSondrioAgent extends CbiGlobeAgent {

    @Inject
    public BPSondrioAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected CbiGlobeRequestBuilder buildRequestBuilder() {
        return new BPSondrioRequestBuilder(
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
        return "https://openapi.popso.it";
    }
}
