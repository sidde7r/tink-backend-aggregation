package se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.authenticator.BnpParibasAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.BnpParibasTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.fetcher.transactionalaccounts.BnpParibasTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.session.BnpParibasSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.bnpparibas.storage.BnpParibasPersistentStorage;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public abstract class BnpParibasAgentBase extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final BnpParibasApiClient apiClient;
    private final BnpParibasPersistentStorage bnpParibasPersistentStorage;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final BnpParibasConfigurationBase configuration;
    private final RandomValueGenerator randomValueGenerator;

    public BnpParibasAgentBase(
            AgentComponentProvider componentProvider, BnpParibasConfigurationBase configuration) {
        super(componentProvider);

        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.configuration = configuration;

        configureHttpClient(this.client, this.configuration);
        this.bnpParibasPersistentStorage = new BnpParibasPersistentStorage(this.persistentStorage);
        this.apiClient =
                new BnpParibasApiClient(
                        this.client, configuration, this.bnpParibasPersistentStorage);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new BnpParibasAuthenticator(
                        apiClient,
                        bnpParibasPersistentStorage,
                        randomValueGenerator,
                        this.configuration));
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

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BnpParibasSessionHandler(apiClient);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        BnpParibasTransactionalAccountFetcher accountFetcher =
                new BnpParibasTransactionalAccountFetcher(apiClient);

        BnpParibasTransactionalAccountTransactionFetcher transactionFetcher =
                new BnpParibasTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .build()));
    }

    private void configureHttpClient(
            TinkHttpClient client, BnpParibasConfigurationBase configuration) {
        client.setUserAgent(configuration.getUserAgent());
    }
}
