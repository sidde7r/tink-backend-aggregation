package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.seb;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseAgent;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;

@AgentCapabilities({Capability.CHECKING_ACCOUNTS, Capability.SAVINGS_ACCOUNTS})
public final class SebEEAgent extends SebBalticsBaseAgent<SebEEApiClient>
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    protected SebEEAgent(AgentComponentProvider componentProvider) {

        super(componentProvider);
        configureHttpClient(client);
        this.apiClient = new SebEEApiClient(client, persistentStorage, request);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new AccessExceededFilter());
    }

    @Override
    protected SebEEApiClient getApiClient() {
        return this.apiClient;
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return transactionalAccountRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return transactionalAccountRefreshController.fetchSavingsTransactions();
    }
}
