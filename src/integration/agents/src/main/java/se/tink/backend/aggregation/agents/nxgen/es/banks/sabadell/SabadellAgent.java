package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell;

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
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.SabadellConstants.Storage;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.SabadellAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.authenticator.entities.SabadellSessionData;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.SabadellCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.creditcards.SabadellCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.SabadellInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.loans.SabadellLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.SabadellAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.transactionalaccounts.SabadellTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.session.SabadellSessionHandler;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
public final class SabadellAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {
    private final SabadellApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final RandomValueGenerator randomValueGenerator;

    @Inject
    public SabadellAgent(final AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.randomValueGenerator = componentProvider.getRandomValueGenerator();
        this.apiClient = new SabadellApiClient(client);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new SabadellInvestmentFetcher(apiClient));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController,
                        updateController,
                        new SabadellLoanFetcher(apiClient));

        this.creditCardRefreshController = constructCreditCardRefreshController();

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
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
                new SabadellAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new SabadellTransactionFetcher(apiClient))));
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
                new SabadellCreditCardFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(
                                new SabadellCreditCardTransactionFetcher(apiClient), 1)));
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
        return new SabadellSessionHandler(apiClient);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return sessionStorage
                .get(Storage.SESSION_KEY, SabadellSessionData.class)
                .orElseThrow(IllegalStateException::new)
                .getSessionResponse()
                .getUser()
                .toTinkIdentity();
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return new SabadellAuthenticator(
                apiClient,
                sessionStorage,
                persistentStorage,
                supplementalInformationFormer,
                randomValueGenerator);
    }
}
