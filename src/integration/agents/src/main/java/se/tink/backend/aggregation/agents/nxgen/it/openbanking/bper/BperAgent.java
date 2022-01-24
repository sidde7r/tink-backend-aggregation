package se.tink.backend.aggregation.agents.nxgen.it.openbanking.bper;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.TRANSFERS;
import static se.tink.backend.aggregation.agents.agentcapabilities.PisCapability.PIS_SEPA_RECURRING_PAYMENTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.PisCapability.SEPA_CREDIT_TRANSFER;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentPisCapability;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.bper.filter.BperPaymentRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.HttpClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, TRANSFERS})
@AgentPisCapability(capabilities = {SEPA_CREDIT_TRANSFER, PIS_SEPA_RECURRING_PAYMENTS})
public final class BperAgent extends CbiGlobeAgent {

    @Inject
    public BperAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        client.addFilter(
                new BperPaymentRetryFilter(
                        HttpClient.MAX_RETRIES,
                        HttpClient.RETRY_SLEEP_MILLISECONDS_SLOW_AUTHENTICATION));
    }

    @Override
    protected String getBaseUrl() {
        return "https://openbanking.bper.it";
    }
}
