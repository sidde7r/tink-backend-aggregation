package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

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
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.StatusUpdater;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.Fetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.authenticator.JyskeBankNemidAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.creditcard.JyskeBankCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.identity.JyskeIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.investment.JyskeBankInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.loan.JyskeBankLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.JyskeBankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.transactionalaccount.JyskeBankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.filters.JyskeBankRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.filters.JyskeBankUnavailableFilter;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.session.JyskeBankSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
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
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.AgentTemporaryStorage;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    LOANS,
    IDENTITY_DATA
})
public class JyskeBankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final JyskeBankApiClient apiClient;
    private final StatusUpdater statusUpdater;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final JyskeIdentityDataFetcher identityDataFetcher;
    private final RandomValueGenerator randomValueGenerator;
    private final JyskeBankPersistentStorage jyskePersistentStorage;
    private final AgentTemporaryStorage agentTemporaryStorage;

    @Inject
    public JyskeBankAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.apiClient = new JyskeBankApiClient(client, sessionStorage, randomValueGenerator);
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.investmentRefreshController = constructInvestmentRefreshController();
        this.loanRefreshController = constructLoanRefreshController();
        this.statusUpdater = componentProvider.getContext();
        this.jyskePersistentStorage = new JyskeBankPersistentStorage(persistentStorage);
        this.identityDataFetcher = new JyskeIdentityDataFetcher(apiClient, jyskePersistentStorage);
        this.agentTemporaryStorage = componentProvider.getAgentTemporaryStorage();
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new JyskeBankSessionHandler(apiClient, jyskePersistentStorage, sessionStorage);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final JyskeBankNemidAuthenticator jyskeBankAuthenticator =
                new JyskeBankNemidAuthenticator(
                        apiClient, jyskePersistentStorage, randomValueGenerator, sessionStorage);

        final NemIdIFrameController iFrameController =
                NemIdIFrameControllerInitializer.initNemIdIframeController(
                        jyskeBankAuthenticator,
                        catalog,
                        statusUpdater,
                        supplementalInformationController,
                        metricContext,
                        agentTemporaryStorage);

        final JyskeBankNemidAuthenticator jyskeBankNemidAuthenticator =
                new JyskeBankNemidAuthenticator(
                        apiClient,
                        jyskePersistentStorage,
                        randomValueGenerator,
                        sessionStorage,
                        iFrameController);

        return new AutoAuthenticationController(
                request, systemUpdater, jyskeBankNemidAuthenticator, jyskeBankAuthenticator);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final JyskeBankAccountFetcher accountFetcher = new JyskeBankAccountFetcher(apiClient);
        final JyskeBankTransactionFetcher transactionFetcher =
                new JyskeBankTransactionFetcher(apiClient);

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
        final JyskeBankCreditCardFetcher jyskeBankCreditCardFetcher =
                new JyskeBankCreditCardFetcher(apiClient);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                jyskeBankCreditCardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                jyskeBankCreditCardFetcher, Fetcher.START_PAGE)));
    }

    private InvestmentRefreshController constructInvestmentRefreshController() {
        final JyskeBankInvestmentFetcher investmentFetcher =
                new JyskeBankInvestmentFetcher(apiClient);

        return new InvestmentRefreshController(
                metricRefreshController, updateController, investmentFetcher);
    }

    private LoanRefreshController constructLoanRefreshController() {
        final JyskeBankLoanFetcher loanFetcher = new JyskeBankLoanFetcher(apiClient);
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

    @Override
    public FetchLoanAccountsResponse fetchLoanAccounts() {
        return loanRefreshController.fetchLoanAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchLoanTransactions() {
        return loanRefreshController.fetchLoanTransactions();
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(identityDataFetcher.fetchIdentityData());
    }

    private void configureHttpClient(final TinkHttpClient client) {
        client.setFollowRedirects(false);
        client.addFilter(new JyskeBankUnavailableFilter());
        client.addFilter(
                new JyskeBankRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
    }
}
