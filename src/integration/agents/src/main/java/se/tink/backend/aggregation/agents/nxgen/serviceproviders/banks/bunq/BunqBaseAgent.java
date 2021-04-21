package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.BunqTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.fetchers.transactional.BunqTransactionalTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.filter.BunqRequiredHeadersFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.filter.BunqSignatureHeaderFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public abstract class BunqBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    protected final String payload;
    private final BunqBaseApiClient apiClient;
    protected TemporaryStorage temporaryStorage;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public BunqBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        payload = Preconditions.checkNotNull(request.getProvider().getPayload());
        this.apiClient = new BunqBaseApiClient(client, getBackendHost());
        temporaryStorage = new TemporaryStorage();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        configureHttpClient(client);
    }

    protected abstract String getBackendHost();

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BunqRequiredHeadersFilter(sessionStorage));
        client.addFilter(
                new BunqSignatureHeaderFilter(
                        sessionStorage, temporaryStorage, client.getUserAgent()));
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new BunqTransactionalAccountFetcher(sessionStorage, apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new BunqTransactionalTransactionsFetcher(
                                        sessionStorage, apiClient))));
    }
}
