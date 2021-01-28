package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.AgentPlatformHttpClient;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agents.agentplatform.authentication.ObjectMapperFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarAuthenticationConfig;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.NemIdIframeControllerAttributes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.LunarTransactionalAccountFetcher;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class LunarDkAgent extends AgentPlatformAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                AgentPlatformAuthenticator {

    private final LunarApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LunarAuthenticationConfig lunarAuthenticationConfig;

    @Inject
    public LunarDkAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        configureHttpClient(client);
        RandomValueGenerator randomValueGenerator =
                agentComponentProvider.getRandomValueGenerator();

        this.apiClient = new LunarApiClient(client, getPersistentStorage(), randomValueGenerator);

        AgentPlatformLunarApiClient agentPlatformLunarApiClient =
                new AgentPlatformLunarApiClient(
                        new AgentPlatformHttpClient(client), randomValueGenerator);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.lunarAuthenticationConfig =
                createLunarAuthenticationConfig(
                        agentComponentProvider, randomValueGenerator, agentPlatformLunarApiClient);
    }

    private LunarAuthenticationConfig createLunarAuthenticationConfig(
            AgentComponentProvider agentComponentProvider,
            RandomValueGenerator randomValueGenerator,
            AgentPlatformLunarApiClient agentPlatformLunarApiClient) {
        return new LunarAuthenticationConfig(
                agentPlatformLunarApiClient,
                new ObjectMapperFactory().getInstance(),
                randomValueGenerator,
                new NemIdIframeControllerAttributes(
                        catalog,
                        agentComponentProvider.getContext(),
                        agentComponentProvider.getSupplementalRequester(),
                        agentComponentProvider.getMetricContext(),
                        agentComponentProvider.getCredentialsRequest().getCredentials()));
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.addFilter(
                new TimeoutRetryFilter(
                        LunarConstants.HttpClient.MAX_RETRIES,
                        LunarConstants.HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new BankServiceInternalErrorFilter());
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
        LunarTransactionalAccountFetcher accountFetcher =
                new LunarTransactionalAccountFetcher(apiClient);

        // Just for now to check if user is authenticated
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(accountFetcher, 1),
                        accountFetcher));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        return lunarAuthenticationConfig.createAuthProcess();
    }

    @Override
    public boolean isBackgroundRefreshPossible() {
        return true;
    }
}
