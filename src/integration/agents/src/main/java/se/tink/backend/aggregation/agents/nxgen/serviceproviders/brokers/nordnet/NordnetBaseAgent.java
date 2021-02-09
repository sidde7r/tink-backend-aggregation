package se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.NordnetBankIdAutoStartAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.authenticator.NordnetPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.investment.NordnetBaseInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.fetcher.transactionalaccount.NordnetBaseTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.brokers.nordnet.session.NordnetBaseSessionHandler;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.TypedAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.bankid.BankIdAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.password.PasswordAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.investment.InvestmentRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public abstract class NordnetBaseAgent<T extends NordnetBaseApiClient> extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    protected final SessionStorage sessionStorage;
    protected T apiClient;
    protected InvestmentRefreshController investmentRefreshController;
    protected TransactionalAccountRefreshController transactionalAccountRefreshController;

    public NordnetBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.sessionStorage = new SessionStorage();
        client.setFollowRedirects(false);
    }

    protected abstract T createApiClient();

    @Override
    protected Authenticator constructAuthenticator() {

        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(
                        supplementalInformationController,
                        new NordnetBankIdAutoStartAuthenticator(apiClient, persistentStorage),
                        persistentStorage,
                        credentials),
                new PasswordAuthenticationController(
                        new NordnetPasswordAuthenticator(apiClient, sessionStorage)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordnetBaseSessionHandler(apiClient);
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
    public FetchInvestmentAccountsResponse fetchInvestmentAccounts() {
        return investmentRefreshController.fetchInvestmentAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchInvestmentTransactions() {
        return investmentRefreshController.fetchInvestmentTransactions();
    }

    protected InvestmentRefreshController constructInvestmentRefreshController() {
        final NordnetBaseInvestmentFetcher investmentFetcher =
                new NordnetBaseInvestmentFetcher(apiClient, sessionStorage);

        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                investmentFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(investmentFetcher)
                                .build()));
    }

    protected TransactionalAccountRefreshController
            constructTransactionalAccountRefreshController() {
        NordnetBaseTransactionalAccountFetcher nordnetBaseTransactionalAccountFetcher =
                new NordnetBaseTransactionalAccountFetcher(apiClient, sessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                nordnetBaseTransactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        nordnetBaseTransactionalAccountFetcher)
                                .build()));
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
    public FetchIdentityDataResponse fetchIdentityData() {
        return apiClient.fetchIdentityData();
    }
}
