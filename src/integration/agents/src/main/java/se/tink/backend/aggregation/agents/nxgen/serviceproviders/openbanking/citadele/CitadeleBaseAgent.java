package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele;

import java.time.temporal.ChronoUnit;
import java.util.Collection;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.Values;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.authenticator.CitadeleBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.configuration.CitadeleBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.CitadeleTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount.CitadeleTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;

public abstract class CitadeleBaseAgent extends SubsequentProgressiveGenerationAgent
        implements RefreshCheckingAccountsExecutor, ProgressiveAuthAgent {

    protected final CitadeleBaseApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final StatelessProgressiveAuthenticator authenticator;
    protected final String clientName;
    private final LocalDateTimeSource localDateTimeSource;

    protected CitadeleBaseAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);
        AgentConfiguration<CitadeleBaseConfiguration> agentConfiguration = getAgentConfiguration();
        this.localDateTimeSource = componentProvider.getLocalDateTimeSource();
        apiClient =
                new CitadeleBaseApiClient(
                        client,
                        persistentStorage,
                        agentConfiguration,
                        componentProvider.getRandomValueGenerator(),
                        getUserIpInformation(),
                        componentProvider.getLocalDateTimeSource().now().toLocalDate());
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        this.clientName = request.getProvider().getPayload();
        authenticator =
                new CitadeleBaseAuthenticator(
                        apiClient,
                        persistentStorage,
                        getApiLocale(request.getUser().getLocale()),
                        componentProvider.getCredentialsRequest().getProvider().getMarket(),
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
        client.addFilter(new AccessExceededFilter());
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

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        CitadeleTransactionalAccountFetcher accountFetcher =
                new CitadeleTransactionalAccountFetcher(apiClient, persistentStorage);
        CitadeleTransactionFetcher transactionFetcher =
                new CitadeleTransactionFetcher(apiClient, request.getProvider().getMarket());

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .setAmountAndUnitToFetch(Values.DAYS_TO_FETCH, ChronoUnit.DAYS)
                                .setConsecutiveEmptyPagesLimit(Values.LIMIT_EMPTY_PAGES)
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    protected abstract Collection<String> getSupportedLocales();

    private String getApiLocale(String userLocale) {
        final String userLanguage = userLocale.split("_")[0];
        return getSupportedLocales().stream()
                .filter(locale -> locale.startsWith(userLanguage))
                .findFirst()
                .orElse("en");
    }
}
