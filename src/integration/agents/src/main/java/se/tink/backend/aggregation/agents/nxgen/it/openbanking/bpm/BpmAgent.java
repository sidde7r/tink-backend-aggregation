package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;
import static se.tink.backend.aggregation.agents.agentcapabilities.PisCapability.PIS_SEPA_RECURRING_PAYMENTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.PisCapability.SEPA_CREDIT_TRANSFER;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bpm.client.BpmFetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.client.CbiGlobeFetcherApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(capabilities = {SEPA_CREDIT_TRANSFER, PIS_SEPA_RECURRING_PAYMENTS})
public final class BpmAgent extends CbiGlobeAgent {

    @Inject
    public BpmAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
    }

    @Override
    protected CbiGlobeFetcherApiClient buildFetcherApiClient() {
        return new BpmFetcherApiClient(cbiRequestBuilder, urlProvider, storage);
    }
}
