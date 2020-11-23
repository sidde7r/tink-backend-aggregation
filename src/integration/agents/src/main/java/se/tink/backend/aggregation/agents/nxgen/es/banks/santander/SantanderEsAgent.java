package se.tink.backend.aggregation.agents.nxgen.es.banks.santander;

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
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.authenticator.SantanderEsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.creditcards.CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.identitydata.SantanderEsIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.investments.SantanderEsInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.loan.SantanderEsLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.SantanderEsAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.fetcher.transactionalaccounts.SantanderEsTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.session.SantanderEsSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.es.banks.santander.utils.SantanderEsBankServiceExceptionFilter;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    IDENTITY_DATA,
    INVESTMENTS,
    LOANS
})
public final class SantanderEsAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final SantanderEsApiClient apiClient;
    private final SantanderEsSessionStorage santanderEsSessionStorage;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public SantanderEsAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);

        santanderEsSessionStorage = new SantanderEsSessionStorage(sessionStorage);
        this.apiClient = new SantanderEsApiClient(client);

        SantanderEsInvestmentFetcher investmentFetcher =
                new SantanderEsInvestmentFetcher(apiClient, santanderEsSessionStorage);
        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController, updateController, investmentFetcher);

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SantanderEsLoanFetcher(apiClient, santanderEsSessionStorage));

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        addExceptionFilters(client);
    }

    protected void addExceptionFilters(TinkHttpClient client) {
        client.addFilter(new SantanderEsBankServiceExceptionFilter());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                new SantanderEsAuthenticator(apiClient, santanderEsSessionStorage));
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
                new SantanderEsAccountFetcher(santanderEsSessionStorage),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new SantanderEsTransactionFetcher(apiClient))));
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
        CreditCardFetcher creditcardFetcher =
                new CreditCardFetcher(apiClient, santanderEsSessionStorage);

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditcardFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(creditcardFetcher)));
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
        return new SantanderEsSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final SantanderEsIdentityDataFetcher fetcher =
                new SantanderEsIdentityDataFetcher(santanderEsSessionStorage);
        return fetcher.response();
    }
}
