package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Headers;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.Sparebank1Authenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1InvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.Sparebank1LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.Sparebank1TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.Sparebank1TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.filters.AddRefererFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.sessionhandler.Sparebank1SessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.no.bankid.BankIdAuthenticationControllerNO;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, LOANS})
public final class Sparebank1Agent extends NextGenerationAgent
        implements RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final Sparebank1ApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private String branchId;

    @Inject
    public Sparebank1Agent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        configureHttpClient(client);

        this.branchId = request.getProvider().getPayload();

        apiClient = new Sparebank1ApiClient(client, branchId);

        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new Sparebank1InvestmentsFetcher(apiClient));

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new Sparebank1LoanFetcher(apiClient));

        creditCardRefreshController = constructCreditCardRefreshController();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(Headers.USER_AGENT);
        AddRefererFilter filter = new AddRefererFilter();
        client.addFilter(filter);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        Sparebank1Authenticator authenticator =
                new Sparebank1Authenticator(apiClient, credentials, persistentStorage, branchId);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new BankIdAuthenticationControllerNO(supplementalRequester, authenticator, catalog),
                authenticator);
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
        Sparebank1TransactionFetcher sparebank1TransactionFetcher =
                new Sparebank1TransactionFetcher(apiClient);
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new Sparebank1TransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        sparebank1TransactionFetcher)
                                .setConsecutiveEmptyPagesLimit(2)
                                .setAmountAndUnitToFetch(6, ChronoUnit.MONTHS)
                                .build()));
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
                metricRefreshController,
                updateController,
                new Sparebank1CreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new Sparebank1CreditCardTransactionFetcher(apiClient))));
    }

    //    Investments are temporarly disabled for Norwegian Agents ITE-1676
    //    @Override
    //    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
    //        return investmentRefreshController.fetchInvestmentAccounts();
    //    }
    //
    //    @Override
    //    public FetchTransactionsResponse fetchInvestmentTransactions() {
    //        return investmentRefreshController.fetchInvestmentTransactions();
    //    }

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
        return new Sparebank1SessionHandler(apiClient);
    }
}
