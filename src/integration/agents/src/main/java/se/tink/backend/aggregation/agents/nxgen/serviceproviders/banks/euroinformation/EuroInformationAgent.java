package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.authentication.EuroInformationPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm.EuroInformationNoPfmCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.creditcard.nopfm.EuroInformationNoPfmCreditCardTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.investment.EuroInformationInvestmentAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional.EuroInformationAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional.notpaginated.EuroInformationTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.fetcher.transactional.paginated.EuroInformationOperationsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.interfaces.EuroInformationApiClientFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.euroinformation.session.EuroInformationSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.account.transactional.TransactionalAccount;

public abstract class EuroInformationAgent extends NextGenerationAgent
        implements RefreshIdentityDataExecutor,
                RefreshInvestmentAccountsExecutor,
                RefreshCreditCardAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor {
    protected final EuroInformationApiClient apiClient;
    private final EuroInformationConfiguration config;
    private final InvestmentRefreshController investmentRefreshController;
    private final CreditCardRefreshController creditCardRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    protected EuroInformationAgent(
            AgentComponentProvider componentProvider, EuroInformationConfiguration config) {
        super(componentProvider);
        this.config = config;
        this.apiClient = new EuroInformationApiClient(this.client, sessionStorage, config);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        EuroInformationInvestmentAccountFetcher.create(
                                this.apiClient, this.sessionStorage));

        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    protected EuroInformationAgent(
            AgentComponentProvider componentProvider,
            EuroInformationConfiguration config,
            EuroInformationApiClientFactory apiClientFactory) {
        super(componentProvider);
        this.config = config;
        this.apiClient = apiClientFactory.getApiClient(client, sessionStorage, config);

        this.investmentRefreshController =
                new InvestmentRefreshController(
                        metricRefreshController,
                        updateController,
                        EuroInformationInvestmentAccountFetcher.create(
                                this.apiClient, this.sessionStorage));

        this.creditCardRefreshController = constructCreditCardRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new PasswordAuthenticationController(
                EuroInformationPasswordAuthenticator.create(
                        this.apiClient, this.sessionStorage, this.config));
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
                EuroInformationAccountFetcher.create(this.apiClient, this.sessionStorage),
                getTransactionFetcher());
    }

    private TransactionFetcher<TransactionalAccount> getTransactionFetcher() {
        if (this.sessionStorage
                .get(EuroInformationConstants.Tags.PFM_ENABLED, Boolean.class)
                .orElse(false)) {
            return new TransactionFetcherController<>(
                    transactionPaginationHelper,
                    new TransactionKeyPaginationController<>(
                            EuroInformationOperationsFetcher.create(this.apiClient)));
        }
        return EuroInformationTransactionsFetcher.create(this.apiClient);
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
                EuroInformationNoPfmCreditCardFetcher.create(this.apiClient, this.sessionStorage),
                EuroInformationNoPfmCreditCardTransactionsFetcher.create(this.apiClient));
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
    protected SessionHandler constructSessionHandler() {
        return EuroInformationSessionHandler.create(this.apiClient, this.config);
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        EuroInformationIdentityFetcher fetcher = new EuroInformationIdentityFetcher(sessionStorage);
        return new FetchIdentityDataResponse(fetcher.fetchIdentityData());
    }
}
