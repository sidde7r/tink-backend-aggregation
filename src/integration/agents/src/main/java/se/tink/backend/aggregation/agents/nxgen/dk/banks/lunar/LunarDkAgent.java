package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar;

import static se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.HeaderValues;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.time.Clock;
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
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.LunarNemIdParametersFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.NemIdIframeAttributes;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.client.AuthenticationApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance.LunarDataAccessorFactory;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.client.FetcherApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.LunarTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.fetchers.transactionalaccount.LunarTransactionalAccountFetcher;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
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

    private final FetcherApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final LunarAuthenticationConfig lunarAuthenticationConfig;
    private final RandomValueGenerator randomValueGenerator;
    private final LunarDataAccessorFactory accessorFactory;

    @Inject
    public LunarDkAgent(AgentComponentProvider agentComponentProvider) {
        super(agentComponentProvider);
        configureHttpClient(client);
        randomValueGenerator = agentComponentProvider.getRandomValueGenerator();
        accessorFactory = new LunarDataAccessorFactory(new ObjectMapperFactory().getInstance());
        String languageCode = HeaderValues.getLanguageCode(request.getUser().getLocale());

        this.apiClient =
                new FetcherApiClient(
                        client,
                        getPersistentStorage(),
                        accessorFactory,
                        randomValueGenerator,
                        languageCode);

        AuthenticationApiClient authenticationApiClient =
                new AuthenticationApiClient(
                        new AgentPlatformHttpClient(client), randomValueGenerator, languageCode);

        this.transactionalAccountRefreshController =
                constructTransactionalAccountRefreshController();

        this.lunarAuthenticationConfig =
                createLunarAuthenticationConfig(agentComponentProvider, authenticationApiClient);
    }

    private LunarAuthenticationConfig createLunarAuthenticationConfig(
            AgentComponentProvider agentComponentProvider,
            AuthenticationApiClient authenticationApiClient) {
        LunarNemIdParametersFetcher parametersFetcher =
                new LunarNemIdParametersFetcher(Clock.systemDefaultZone());
        return new LunarAuthenticationConfig(
                authenticationApiClient,
                accessorFactory,
                randomValueGenerator,
                new NemIdIframeAttributes(
                        parametersFetcher,
                        catalog,
                        agentComponentProvider.getContext(),
                        supplementalInformationController,
                        metricContext,
                        credentials));
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
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new LunarTransactionalAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                new LunarTransactionFetcher(apiClient))));
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
