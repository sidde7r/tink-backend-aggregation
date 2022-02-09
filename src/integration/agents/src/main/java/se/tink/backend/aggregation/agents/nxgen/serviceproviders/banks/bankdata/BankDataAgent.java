package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.authenticator.BankDataNemidAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.creditcard.BankDataCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.identity.BankDataIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.investment.BankDataInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.loan.BankDataLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount.BankDataAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.transactionalaccount.BankDataTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.filters.BankDataRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.filters.BankDataUnavailableFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.session.BankDataSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdCredentialsProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.nemid.ss.NemIdIFrameControllerInitializer;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.http.client.LoggingStrategy;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;

public abstract class BankDataAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final NemIdIFrameControllerInitializer iFrameControllerInitializer;
    private final BankDataApiClient apiClient;
    private final StatusUpdater statusUpdater;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final BankDataIdentityDataFetcher identityDataFetcher;
    private final RandomValueGenerator randomValueGenerator;
    private final BankDataPersistentStorage bankDataPersistentStorage;
    private final AgentTemporaryStorage agentTemporaryStorage;
    private final BankDataConfiguration configuration;

    @Inject
    public BankDataAgent(
            AgentComponentProvider componentProvider,
            NemIdIFrameControllerInitializer iFrameControllerInitializer) {
        super(componentProvider);

        setJsonHttpTrafficLogsEnabled(true);
        client.setLoggingStrategy(LoggingStrategy.EXPERIMENTAL);

        this.configuration = createConfiguration();
        configureHttpClient(client);
        this.iFrameControllerInitializer = iFrameControllerInitializer;
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.apiClient =
                new BankDataApiClient(client, sessionStorage, randomValueGenerator, configuration);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.investmentRefreshController = constructInvestmentRefreshController();
        this.loanRefreshController = constructLoanRefreshController();
        this.statusUpdater = componentProvider.getContext();
        this.bankDataPersistentStorage = new BankDataPersistentStorage(persistentStorage);
        this.identityDataFetcher =
                new BankDataIdentityDataFetcher(apiClient, bankDataPersistentStorage);
        this.agentTemporaryStorage = componentProvider.getAgentTemporaryStorage();
    }

    protected abstract BankDataConfiguration createConfiguration();

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BankDataSessionHandler(apiClient, bankDataPersistentStorage, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final BankDataNemidAuthenticator bankDataAuthenticator =
                new BankDataNemidAuthenticator(
                        apiClient, bankDataPersistentStorage, randomValueGenerator, sessionStorage);

        final NemIdIFrameController iFrameController =
                iFrameControllerInitializer.initNemIdIframeController(
                        bankDataAuthenticator,
                        NemIdCredentialsProvider.defaultProvider(),
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext,
                        agentTemporaryStorage);

        final BankDataNemidAuthenticator bankDataNemidAuthenticator =
                new BankDataNemidAuthenticator(
                        apiClient,
                        bankDataPersistentStorage,
                        randomValueGenerator,
                        sessionStorage,
                        iFrameController);

        return new AutoAuthenticationController(
                request, systemUpdater, bankDataNemidAuthenticator, bankDataAuthenticator);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final BankDataAccountFetcher accountFetcher = new BankDataAccountFetcher(apiClient);
        final BankDataTransactionFetcher transactionFetcher =
                new BankDataTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                transactionFetcher, Fetcher.START_PAGE)));
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController() {
        final BankDataCreditCardFetcher bankDataCreditCardFetcher =
                new BankDataCreditCardFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                bankDataCreditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                bankDataCreditCardFetcher, Fetcher.START_PAGE)));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        final BankDataInvestmentFetcher investmentFetcher =
                new BankDataInvestmentFetcher(apiClient);

        return new InvestmentRefreshController(
                metricRefreshController, updateController, investmentFetcher);
    }

    private LoanRefreshController constructLoanRefreshController() {
        final BankDataLoanFetcher loanFetcher = new BankDataLoanFetcher(apiClient);
        final TransactionFetcherController<LoanAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(loanFetcher, Fetcher.START_PAGE));

        return new LoanRefreshController(
                metricRefreshController,
                updateController,
                loanFetcher,
                transactionFetcherController);
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
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityDataFetcher.fetchIdentityData());
    }

    private void configureHttpClient(final TinkHttpClient client) {
        client.setFollowRedirects(false);
        client.addFilter(new BankDataUnavailableFilter());
        client.addFilter(
                new BankDataRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
    }
}
