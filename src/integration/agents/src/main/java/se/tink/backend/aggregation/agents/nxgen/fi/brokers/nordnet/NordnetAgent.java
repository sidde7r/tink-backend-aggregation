package se.tink.backend.aggregation.agents.nxgen.fi.brokers.nordnet;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.NordnetBaseApiClient;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

@AgentCapabilities({SAVINGS_ACCOUNTS, INVESTMENTS, IDENTITY_DATA})
public class NordnetAgent extends NordnetBaseAgent {

    @Inject
    public NordnetAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.apiClient = createApiClient();
        this.investmentRefreshController = constructInvestmentRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected NordnetBaseApiClient createApiClient() {
        return new NordnetApiClient(client, credentials, persistentStorage, sessionStorage);
    }
}
