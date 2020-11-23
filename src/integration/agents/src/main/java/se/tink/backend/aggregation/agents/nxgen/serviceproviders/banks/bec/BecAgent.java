package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.LOANS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
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
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.authenticator.BecSecurityHelper;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.investment.BecInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.loan.BecLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.session.BecSessionHandler;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, CREDIT_CARDS, INVESTMENTS, LOANS})
public final class BecAgent extends SubsequentProgressiveGenerationAgent
        implements ProgressiveAuthAgent,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {

    private final BecApiClient apiClient;
    private final BecAccountTransactionsFetcher transactionFetcher;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public BecAgent(AgentComponentProvider agentComponentProvider) {

        super(agentComponentProvider);

        BecSecurityHelper securityHelper =
                new BecSecurityHelper(
                        new String(
                                EncodingUtils.decodeBase64String(
                                        BecConstants.Crypto.SIGNING_CERTIFICATE_B64)),
                        BecConstants.Crypto.PUBLIC_KEY_SALT);

        this.apiClient =
                new BecApiClient(
                        securityHelper,
                        this.client,
                        new BecUrlConfiguration(request.getProvider().getPayload()),
                        catalog);

        this.transactionFetcher = new BecAccountTransactionsFetcher(this.apiClient);
        this.investmentRefreshController =
                new InvestmentRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new BecInvestmentFetcher(this.apiClient));
        this.loanRefreshController =
                new LoanRefreshController(
                        this.metricRefreshController,
                        this.updateController,
                        new BecLoanFetcher(this.apiClient, this.credentials));
        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.client.setResponseStatusHandler(new BecResponseStatusHandler());
        client.setTimeout(60 * 1000); // increase standard 30sec into 1 minute
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
                        new TransactionDatePaginationController<>(this.transactionFetcher),
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
                        new TransactionDatePaginationController<>(
                                new BecCreditCardTransactionsFetcher(this.apiClient))));
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

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new BecAuthenticator(
                this.apiClient, sessionStorage, supplementalRequester, persistentStorage, catalog);
    }
}
