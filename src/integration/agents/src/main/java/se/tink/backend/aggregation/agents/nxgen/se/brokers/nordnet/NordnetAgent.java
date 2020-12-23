package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.filters.NordnetFoundRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentCapabilities({SAVINGS_ACCOUNTS, INVESTMENTS, IDENTITY_DATA})
public class NordnetAgent extends NordnetBaseAgent {

    @Inject
    public NordnetAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = createApiClient();
        this.investmentRefreshController = constructInvestmentRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        addFilters(client);
    }

    private void addFilters(TinkHttpClient client) {
        client.addFilter(
                new NordnetFoundRetryFilter(
                        NordnetBaseConstants.NordnetRetryFilter.NUM_TIMEOUT_RETRIES,
                        NordnetBaseConstants.NordnetRetryFilter.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    protected NordnetBaseApiClient createApiClient() {
        return new NordnetApiClient(client, credentials, persistentStorage, sessionStorage);
    }
}
