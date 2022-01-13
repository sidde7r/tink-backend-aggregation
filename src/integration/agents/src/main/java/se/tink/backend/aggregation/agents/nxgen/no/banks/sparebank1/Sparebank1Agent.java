package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LOANS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.time.temporal.ChronoUnit;
import org.apache.http.client.config.CookieSpecs;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.Sparebank1Constants.Headers;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.authenticator.Sparebank1Authenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.Sparebank1TransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.account.Sparebank1TransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.Sparebank1CreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.creditcards.Sparebank1CreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.identity.Sparebank1identityFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.investment.Sparebank1InvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.fetcher.loan.Sparebank1LoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.filters.AddRefererFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.filters.ServerErrorFilter;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebank1.filters.ServerErrorRetryFilter;
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

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, LOANS, IDENTITY_DATA})
public final class Sparebank1Agent extends NextGenerationAgent
        implements RefreshLoanAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {
    private final Sparebank1ApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final String branchId;

    @Inject
    public Sparebank1Agent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);

        this.branchId = componentProvider.getProvider().getPayload();

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
        client.addFilter(new AddRefererFilter());
        client.addFilter(new ServerErrorFilter());
        client.addFilter(new ServerErrorRetryFilter());
        client.setCookieSpec(CookieSpecs.STANDARD);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        Sparebank1Authenticator authenticator =
                new Sparebank1Authenticator(apiClient, credentials, persistentStorage, branchId);

        int authenticationPollIntervalSeconds = 10000;
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new BankIdAuthenticationControllerNO(
                        supplementalInformationController,
                        authenticator,
                        catalog,
                        authenticationPollIntervalSeconds),
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

    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

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

    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

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
        return new Sparebank1SessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                new Sparebank1identityFetcher(persistentStorage).fetchIdentityData());
    }
}
