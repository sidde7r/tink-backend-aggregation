package se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.SparebankenVestAutoAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.authenticator.SparebankenVestOneTimeActivationCodeAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.SparebankenVestCreditCardAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.creditcard.SparebankenVestCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.investment.SparebankenVestInvestmentsFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.loan.SparebankenVestLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.SparebankenVestTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.fetcher.transactionalaccount.SparebankenVestTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.banks.sparebankenvest.session.SparebankenVestSessionHandler;
import se.tink.backend.aggregation.agents.utils.authentication.encap3.EncapClient;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.utils.deviceprofile.DeviceProfileConfiguration;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, LOANS})
public final class SparebankenVestAgent extends NextGenerationAgent
        implements RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final SparebankenVestApiClient apiClient;
    private final EncapClient encapClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public SparebankenVestAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        configureHttpClient(client);
        this.apiClient = new SparebankenVestApiClient(client);
        this.encapClient =
                agentComponentProvider.getEncapClient(
                        persistentStorage,
                        new SparebankenVestEncapConfiguration(),
                        DeviceProfileConfiguration.IOS_STABLE,
                        client);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        SparebankenVestInvestmentsFetcher.create(apiClient));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        SparebankenVestLoanFetcher.create(apiClient));

        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.setUserAgent(SparebankenVestConstants.Headers.USER_AGENT);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new PasswordAuthenticationController(
                        SparebankenVestOneTimeActivationCodeAuthenticator.create(
                                apiClient, encapClient, sessionStorage)),
                SparebankenVestAutoAuthenticator.create(apiClient, encapClient, sessionStorage));
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
        SparebankenVestTransactionFetcher transactionFetcher =
                SparebankenVestTransactionFetcher.create(apiClient);

        TransactionFetcherController<TransactionalAccount> transactionFetcherController =
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                transactionFetcher,
                                SparebankenVestConstants.PagePagination
                                        .CONSECUTIVE_EMPTY_PAGES_LIMIT),
                        transactionFetcher);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                SparebankenVestTransactionalAccountFetcher.create(apiClient, credentials),
                transactionFetcherController);
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

        TransactionFetcher<CreditCardAccount> transactionFetcher =
                new TransactionFetcherController<CreditCardAccount>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                SparebankenVestCreditCardTransactionFetcher.create(apiClient)));

        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                SparebankenVestCreditCardAccountFetcher.create(apiClient),
                transactionFetcher);
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
        return SparebankenVestSessionHandler.create(apiClient);
    }
}
