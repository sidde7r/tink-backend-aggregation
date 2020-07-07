package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.NordeaNoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.AuthenticationClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.BaseClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.ExceptionFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.client.FetcherClient;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.creditcard.CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.identitydata.NordeaNoIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.fetcher.transactionalaccount.TransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NordeaNoAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor {

    private final NordeaNoStorage storage;

    private final AuthenticationClient authenticationClient;
    private final FetcherClient fetcherClient;

    private final NordeaNoAuthenticator authenticator;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public NordeaNoAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.storage = new NordeaNoStorage(persistentStorage, sessionStorage);

        componentProvider.getTinkHttpClient().addFilter(new ExceptionFilter());

        BaseClient baseClient = new BaseClient(componentProvider.getTinkHttpClient(), storage);
        this.authenticationClient = new AuthenticationClient(baseClient, storage);
        this.fetcherClient = new FetcherClient(baseClient);

        this.authenticator =
                new NordeaNoAuthenticator(
                        authenticationClient, storage, componentProvider.getRandomValueGenerator());

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        creditCardRefreshController = constructCreditCardRefreshController();
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        TransactionalAccountFetcher accountFetcher = new TransactionalAccountFetcher(fetcherClient);
        TransactionalAccountTransactionFetcher transactionFetcher =
                new TransactionalAccountTransactionFetcher(fetcherClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        CreditCardFetcher creditCardFetcher = new CreditCardFetcher(fetcherClient);
        CreditCardTransactionFetcher creditCardTransactionFetcher =
                new CreditCardTransactionFetcher(fetcherClient);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                creditCardTransactionFetcher, 1)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordeaNoSessionHandler(authenticationClient, storage);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new NordeaNoIdentityDataFetcher(fetcherClient).fetchIdentityData());
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }
}
