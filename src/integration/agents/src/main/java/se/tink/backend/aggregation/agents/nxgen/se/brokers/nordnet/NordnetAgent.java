package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.INVESTMENTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchInvestmentAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshInvestmentAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.NordnetBankIdAutoStartAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.authenticator.NordnetPasswordAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.investment.NordnetInvestmentFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.fetcher.transactionalaccount.NordnetTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.session.NordnetSessionHandler;
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

@AgentCapabilities({SAVINGS_ACCOUNTS, INVESTMENTS, IDENTITY_DATA})
public final class NordnetAgent extends NextGenerationAgent
        implements RefreshInvestmentAccountsExecutor,
                RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final SessionStorage sessionStorage;
    protected final NordnetApiClient apiClient;
    private final InvestmentRefreshController investmentRefreshController;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public NordnetAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        this.sessionStorage = new SessionStorage();
        this.apiClient =
                new NordnetApiClient(client, credentials, persistentStorage, sessionStorage);
        this.investmentRefreshController = constructInvestmentRefreshController();
        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();
        client.setFollowRedirects(false);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        return new TypedAuthenticationController(
                new BankIdAuthenticationController<>(
                        supplementalRequester,
                        new NordnetBankIdAutoStartAuthenticator(apiClient, persistentStorage),
                        persistentStorage,
                        credentials),
                new PasswordAuthenticationController(
                        new NordnetPasswordAuthenticator(apiClient, sessionStorage)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new NordnetSessionHandler(apiClient);
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

    private InvestmentRefreshController constructInvestmentRefreshController() {
        final NordnetInvestmentFetcher investmentFetcher =
                new NordnetInvestmentFetcher(apiClient, sessionStorage);

        return new InvestmentRefreshController(
                metricRefreshController,
                updateController,
                investmentFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(investmentFetcher)));
    }

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        NordnetTransactionalAccountFetcher nordnetTransactionalAccountFetcher =
                new NordnetTransactionalAccountFetcher(apiClient, sessionStorage);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                nordnetTransactionalAccountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                nordnetTransactionalAccountFetcher)));
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
