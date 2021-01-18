package se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.MORTGAGE_AGGREGATION;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.authenticator.IngMultifactorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngLoanAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.fetcher.IngTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ing.v195.session.IngSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionMonthPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.loan.LoanAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS,
    MORTGAGE_AGGREGATION
})
public final class IngAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final IngApiClient ingApiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final IngMultifactorAuthenticator authenticator;

    @Inject
    public IngAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        configureHttpClient(this.client);
        this.ingApiClient = new IngApiClient(this.client);

        this.authenticator =
                new IngMultifactorAuthenticator(
                        ingApiClient,
                        componentProvider.getRandomValueGenerator(),
                        sessionStorage,
                        persistentStorage,
                        componentProvider.getCredentialsRequest(),
                        componentProvider.getSupplementalInformationHelper());
        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new IngInvestmentAccountFetcher(ingApiClient));
        this.loanRefreshController = constructLoanRefreshController();
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        /* SCA status polling takes up to 60s */
        client.setTimeout(61000);
        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return authenticator;
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

        IngTransactionalAccountFetcher accountFetcher =
                new IngTransactionalAccountFetcher(ingApiClient, sessionStorage);
        IngTransactionFetcher transactionFetcher = new IngTransactionFetcher(ingApiClient);

        TransactionMonthPaginationController<TransactionalAccount> paginationController =
                new TransactionMonthPaginationController<>(
                        transactionFetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<TransactionalAccount> fetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, paginationController);

        TransactionalAccountRefreshController refreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        fetcherController);

        return refreshController;
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
        IngCreditCardAccountFetcher accountFetcher = new IngCreditCardAccountFetcher(ingApiClient);
        IngTransactionFetcher transactionFetcher = new IngTransactionFetcher(ingApiClient);

        TransactionMonthPaginationController<CreditCardAccount> paginationController =
                new TransactionMonthPaginationController<>(
                        transactionFetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<CreditCardAccount> fetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, paginationController);

        CreditCardRefreshController refreshController =
                new CreditCardRefreshController(
                        metricRefreshController,
                        updateController,
                        accountFetcher,
                        fetcherController);

        return refreshController;
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

    private LoanRefreshController constructLoanRefreshController() {
        IngLoanAccountFetcher accountFetcher = new IngLoanAccountFetcher(ingApiClient);
        IngTransactionFetcher transactionFetcher = new IngTransactionFetcher(ingApiClient);

        TransactionMonthPaginationController<LoanAccount> paginationController =
                new TransactionMonthPaginationController<>(
                        transactionFetcher, IngConstants.ZONE_ID);

        TransactionFetcherController<LoanAccount> fetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper, paginationController);

        return new LoanRefreshController(
                metricRefreshController, updateController, accountFetcher, fetcherController);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler(ingApiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new IngIdentityDataFetcher(ingApiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
