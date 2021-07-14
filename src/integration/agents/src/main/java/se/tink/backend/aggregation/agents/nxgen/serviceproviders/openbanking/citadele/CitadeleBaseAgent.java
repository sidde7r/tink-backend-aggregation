package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import java.time.temporal.ChronoUnit;
import se.tink.backend.agents.rpc.Field.Key;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstans.Values;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.CitadeleBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration.CitadeleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration.CitadeleMarketConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.CitadeleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.CitadeleTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.filter.CitadeleRetryFilter;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.randomretry.RateLimitRetryFilter;
import se.tink.libraries.identitydata.IdentityData;

public abstract class CitadeleBaseAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshIdentityDataExecutor,
                CitadeleMarketConfiguration,
                ProgressiveAuthAgent {

    protected final CitadeleBaseApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final StatelessProgressiveAuthenticator authenticator;
    private final String providerMarket;
    protected final String clientName;

    protected CitadeleBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        AgentConfiguration<CitadeleBaseConfiguration> agentConfiguration = getAgentConfiguration();
        apiClient =
                new CitadeleBaseApiClient(
                        client,
                        persistentStorage,
                        agentConfiguration,
                        componentProvider.getRandomValueGenerator(),
                        getUserIpInformation(),
                        componentProvider.getLocalDateTimeSource().now().toLocalDate());
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.providerMarket = componentProvider.getCredentialsRequest().getProvider().getMarket();
        clientName = request.getProvider().getPayload();

        authenticator =
                new CitadeleBaseAuthenticator(
                        apiClient,
                        persistentStorage,
                        this,
                        providerMarket,
                        strongAuthenticationState,
                        credentials);
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        return authenticator;
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private CitadeleUserIpInformation getUserIpInformation() {
        return new CitadeleUserIpInformation(
                request.getUserAvailability().isUserPresent(),
                request.getUserAvailability().getOriginatingUserIp());
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(
                new RateLimitRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new AccessExceededFilter());
        client.addFilter(
                new CitadeleRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
    }

    private AgentConfiguration<CitadeleBaseConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(CitadeleBaseConfiguration.class);
    }

    @Override
    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        return new FetchIdentityDataResponse(
                IdentityData.builder()
                        .setFullName(credentials.getField(Key.USERNAME))
                        .setDateOfBirth(null)
                        .build());
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        CitadeleTransactionalAccountFetcher accountFetcher =
                new CitadeleTransactionalAccountFetcher(apiClient);
        CitadeleTransactionFetcher transactionFetcher =
                new CitadeleTransactionFetcher(apiClient, providerMarket);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .setAmountAndUnitToFetch(Values.DAYS_TO_FETCH, ChronoUnit.DAYS)
                                .setConsecutiveEmptyPagesLimit(Values.LIMIT_EMPTY_PAGES)
                                .build()));
    }
}
