package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LOANS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.BecAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.checking.BecAccountTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.BecCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.accounts.creditcard.BecCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticatorModule;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.BecBankUnavailableErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.BecBankUnavailableRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.filter.ScaAuthenticationErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.BecInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.BecLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.session.BecSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
public final class BecAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final BecApiClient apiClient;
    private final BecStorage storage;
    private final BecAccountTransactionsFetcher transactionFetcher;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final RandomValueGenerator randomValueGenerator;
    private final User user;

    @Inject
    public BecAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);

        this.apiClient = createBecApiClient();
        this.storage = new BecStorage(persistentStorage);

        this.transactionFetcher = new BecAccountTransactionsFetcher(this.apiClient);
        this.investmentRefreshController = createInvestmentRefreshController();
        this.loanRefreshController = createLoanRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        configureClient();

        this.randomValueGenerator = agentComponentProvider.getRandomValueGenerator();
        this.user = agentComponentProvider.getUser();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        BecAuthenticatorModule authenticatorModule =
                new BecAuthenticatorModule(
                        apiClient,
                        credentials,
                        storage,
                        user,
                        catalog,
                        supplementalInformationController,
                        randomValueGenerator);
        return authenticatorModule.createAuthenticator();
    }

    private void configureClient() {
        this.client.setTimeout(60 * 1000); // increase standard 30sec into 1 minute
        this.client.addFilter(new ScaAuthenticationErrorFilter());
        this.client.addFilter(new BecBankUnavailableErrorFilter());
        this.client.addFilter(
                new BecBankUnavailableRetryFilter(
                        5, 3000)); // increase standard 30sec into 1 minute
    }

    private BecApiClient createBecApiClient() {
        return new BecApiClient(
                new BecSecurityHelper(),
                client,
                new BecUrlConfiguration(request.getProvider().getPayload()),
                catalog);
    }

    private InvestmentRefreshController createInvestmentRefreshController() {
        return new InvestmentRefreshController(
                this.metricRefreshController,
                this.updateController,
                new BecInvestmentFetcher(this.apiClient));
    }

    private LoanRefreshController createLoanRefreshController() {
        return new LoanRefreshController(
                this.metricRefreshController,
                this.updateController,
                new BecLoanFetcher(this.apiClient));
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
                this.metricRefreshController,
                this.updateController,
                new BecAccountFetcher(this.apiClient),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(this.transactionFetcher)
                                .build(),
                        this.transactionFetcher));
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
        return new CreditCardRefreshController(
                this.metricRefreshController,
                this.updateController,
                new BecCreditCardFetcher(this.apiClient),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new BecCreditCardTransactionsFetcher(this.apiClient))
                                .build()));
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
    protected SessionHandler constructSessionHandler() {
        return new BecSessionHandler(this.apiClient);
    }
}
