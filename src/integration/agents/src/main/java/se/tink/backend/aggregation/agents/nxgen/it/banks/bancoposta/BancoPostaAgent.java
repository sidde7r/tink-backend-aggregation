package se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.authenticator.BancoPostaStorage;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.BancoPostaCheckingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.BancoPostaSavingTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.banks.bancoposta.fetcher.identity.BancoPostaIdentityFetcher;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities
public class BancoPostaAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final BancoPostaApiClient apiClient;
    private final BancoPostaStorage storage;
    private final TransactionalAccountRefreshController checkingAccountRefreshController;
    private final TransactionalAccountRefreshController savingAccountsRefreshController;

    @Inject
    public BancoPostaAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        this.storage = new BancoPostaStorage(persistentStorage);
        this.apiClient = new BancoPostaApiClient(client, storage);
        this.checkingAccountRefreshController = constructCheckingAccountTransactionController();
        this.savingAccountsRefreshController = constructSavingsAccountTransactionController();
    }

    private TransactionalAccountRefreshController constructCheckingAccountTransactionController() {
        BancoPostaCheckingTransactionalAccountFetcher transactionalAccountFetcher =
                new BancoPostaCheckingTransactionalAccountFetcher(this.apiClient);

        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionalAccountFetcher, 0)));
    }

    private TransactionalAccountRefreshController constructSavingsAccountTransactionController() {
        BancoPostaSavingTransactionalAccountFetcher transactionalAccountFetcher =
                new BancoPostaSavingTransactionalAccountFetcher(this.apiClient);

        return new TransactionalAccountRefreshController(
                this.metricRefreshController,
                this.updateController,
                transactionalAccountFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionalAccountFetcher, 0)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new BancoPostaAuthenticator(apiClient, storage, catalog);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return checkingAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return checkingAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public FetchAccountsResponse fetchSavingsAccounts() {
        return savingAccountsRefreshController.fetchSavingsAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchSavingsTransactions() {
        return savingAccountsRefreshController.fetchSavingsTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        BancoPostaIdentityFetcher identityFetcher = new BancoPostaIdentityFetcher(apiClient);
        return new FetchIdentityDataResponse(identityFetcher.fetchIdentityData());
    }
}
