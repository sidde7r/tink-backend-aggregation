package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.MORTGAGE_AGGREGATION;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import org.apache.http.client.config.CookieSpecs;
import se.tink.backend.agents.rpc.Field;
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
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaConstants.RetryFilterValues;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.LaCaixaManualAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.authenticator.LaCaixaMultifactorAuthenticatorController;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.creditcard.LaCaixaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.identitydata.LaCaixaIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.investments.LaCaixaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.loan.LaCaixaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.LaCaixaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.fetcher.transactionalaccount.LaCaixaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.filter.LaCaixaRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.filter.LaCaixaSessionExpiredFilter;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.session.LaCaixaSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.DefaultCookieAwareResponseStatusHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.proxy.ProxyProfile;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA, MORTGAGE_AGGREGATION})
public final class LaCaixaAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final LaCaixaApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final StatelessProgressiveAuthenticator authenticator;

    @Inject
    public LaCaixaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client, componentProvider.getProxyProfiles().getMarketProxyProfile());
        apiClient =
                new LaCaixaApiClient(
                        client,
                        persistentStorage,
                        credentials.getField(Field.Key.USERNAME),
                        componentProvider.getRandomValueGenerator(),
                        sessionStorage);

        LaCaixaInvestmentFetcher investmentFetcher = new LaCaixaInvestmentFetcher(apiClient);
        investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController, updateController, investmentFetcher);

        loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new LaCaixaLoanFetcher(apiClient));

        creditCardRefreshController = constructCreditCardRefreshController();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();

        authenticator = constructAuthenticator();
    }

    private StatelessProgressiveAuthenticator constructAuthenticator() {
        LaCaixaManualAuthenticator laCaixaManualAuthenticator =
                new LaCaixaManualAuthenticator(
                        apiClient,
                        persistentStorage,
                        logMasker,
                        supplementalInformationFormer,
                        catalog,
                        credentials,
                        supplementalInformationHelper);

        return new LaCaixaMultifactorAuthenticatorController(laCaixaManualAuthenticator);
    }

    private void configureHttpClient(TinkHttpClient client, ProxyProfile proxyProfile) {
        client.addFilter(new TimeoutFilter());
        client.addFilter(
                new LaCaixaRetryFilter(
                        RetryFilterValues.MAX_ATTEMPTS,
                        RetryFilterValues.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new LaCaixaSessionExpiredFilter(persistentStorage));
        client.setResponseStatusHandler(
                new DefaultCookieAwareResponseStatusHandler(sessionStorage));
        client.setCookieSpec(CookieSpecs.STANDARD);
        client.setProxyProfile(proxyProfile);
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
        LaCaixaAccountFetcher accountFetcher = new LaCaixaAccountFetcher(apiClient);
        LaCaixaTransactionFetcher transactionFetcher = new LaCaixaTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 0)));
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
        LaCaixaCreditCardFetcher creditCardFetcher = new LaCaixaCreditCardFetcher(apiClient);
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                creditCardFetcher,
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionPagePaginationController<>(creditCardFetcher, 0)));
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
        return new LaCaixaSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new LaCaixaIdentityDataFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
