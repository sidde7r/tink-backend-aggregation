package se.tink.backend.aggregation.agents.nxgen.ee.openbanking.seb;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseAgent;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.SebBalticsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.fetcher.transactionalaccount.SebBalticsTransactionalAccountFetcher;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;

@AgentCapabilities({Capability.CHECKING_ACCOUNTS})
public final class SebEeAgent extends SebBalticsBaseAgent<SebEeApiClient>
        implements RefreshCheckingAccountsExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    protected SebEeAgent(AgentComponentProvider componentProvider) {

        super(componentProvider);
        configureHttpClient(client);
        this.apiClient = new SebEeApiClient(client, persistentStorage, request);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new AccessExceededFilter());
    }

    protected SebEeApiClient getApiClient() {
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

    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        SebBalticsTransactionalAccountFetcher accountFetcher =
                new SebBalticsTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new SebBalticsTransactionFetcher(
                                        apiClient, transactionPaginationHelper))));
    }
}
