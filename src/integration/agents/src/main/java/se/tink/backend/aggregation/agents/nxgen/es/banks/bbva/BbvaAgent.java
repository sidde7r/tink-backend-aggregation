package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CREDIT_CARDS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.LOANS;
import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchLoanAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshLoanAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.BbvaAccountsProvider;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.BbvaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.BbvaCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.creditcard.BbvaCreditCardTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.identitydata.BbvaIdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.investment.BbvaInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.loan.BbvaLoanFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.BbvaAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.fetcher.transactionalaccount.BbvaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.session.BbvaSessionHandler;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.AccountsProvider;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.CreditCardRefreshControllerTransactionsFetchingDateFromManagerAware;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.TransactionalAccountRefreshControllerTransactionsFetchingDateFromManagerAware;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.transactionsdatefrommanager.TransactionsFetchingDateFromManager;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.AccountFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.loan.LoanRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.creditcard.CreditCardAccount;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

@AgentCapabilities({
    CHECKING_ACCOUNTS,
    SAVINGS_ACCOUNTS,
    CREDIT_CARDS,
    //    INVESTMENTS,
    IDENTITY_DATA,
    LOANS
})
@Slf4j
public final class BbvaAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                //                RefreshInvestmentAccountsExecutor,
                RefreshLoanAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    private final BbvaApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final LoanRefreshController loanRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final TransactionsFetchingDateFromManager transactionsFetchingDateFromManager;
    private final AccountsProvider accountsProvider;

    @Inject
    public BbvaAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        client.setProxyProfile(componentProvider.getProxyProfiles().getMarketProxyProfile());
        this.apiClient = new BbvaApiClient(client, sessionStorage, supplementalInformationHelper);
        BbvaAccountFetcher accountFetcher = new BbvaAccountFetcher(apiClient);
        BbvaCreditCardFetcher creditCardFetcher = new BbvaCreditCardFetcher(apiClient);
        accountsProvider = new BbvaAccountsProvider(accountFetcher, creditCardFetcher);
        this.transactionsFetchingDateFromManager =
                new TransactionsFetchingDateFromManager(
                        accountsProvider, transactionPaginationHelper, persistentStorage);
        apiClient.setTransactionsFetchingDateFromManager(transactionsFetchingDateFromManager);
        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        new BbvaInvestmentFetcher(apiClient));

        this.loanRefreshController =
                new LoanRefreshController(
                        metricRefreshController, updateController, new BbvaLoanFetcher(apiClient));

        this.creditCardRefreshController = constructCreditCardRefreshController(creditCardFetcher);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController(accountFetcher);
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
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
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
    public FetchIdentityDataResponse fetchIdentityData() {
        final IdentityDataFetcher fetcher = new BbvaIdentityDataFetcher(apiClient);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        Authenticator authenticator =
                new BbvaAuthenticator(
                        apiClient,
                        supplementalInformationHelper,
                        request,
                        transactionsFetchingDateFromManager,
                        accountsProvider);
        log.info(
                "Credentials status after authenticating is equal {}",
                this.credentials.getStatus());
        return authenticator;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new BbvaSessionHandler(apiClient);
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController(
            AccountFetcher<TransactionalAccount> accountFetcher) {
        return new TransactionalAccountRefreshControllerTransactionsFetchingDateFromManagerAware(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new BbvaTransactionFetcher(apiClient))),
                transactionsFetchingDateFromManager);
    }

    private CreditCardRefreshController constructCreditCardRefreshController(
            AccountFetcher<CreditCardAccount> accountFetcher) {
        return new CreditCardRefreshControllerTransactionsFetchingDateFromManagerAware(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new BbvaCreditCardTransactionFetcher(apiClient))),
                transactionsFetchingDateFromManager);
    }
}
