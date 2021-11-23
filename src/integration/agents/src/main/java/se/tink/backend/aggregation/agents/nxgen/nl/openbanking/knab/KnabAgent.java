package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import org.apache.http.client.config.CookieSpecs;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator.KnabAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.configuration.KnabConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.KnabAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.KnabTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter.KnabFailureFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.filter.KnabRetryFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.time.KnabTimeProvider;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.GatewayTimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TerminatedHandshakeRetryFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.ConnectionTimeoutRetryFilter;

@AgentCapabilities({CHECKING_ACCOUNTS})
public class KnabAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private static final int MAX_RETRIES = 3;

    private static final int RETRY_SLEEP_MILLISECONDS = 3000;

    private final KnabStorage storage;

    private final KnabApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public KnabAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        configureHttpClient(client);

        storage = new KnabStorage(persistentStorage);

        apiClient =
                new KnabApiClient(
                        client,
                        componentProvider.getRandomValueGenerator(),
                        new KnabTimeProvider(componentProvider.getLocalDateTimeSource()),
                        storage,
                        userIp);

        transactionalAccountRefreshController =
                createTransactionalAccountRefreshController(
                        componentProvider.getLocalDateTimeSource());
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(new KnabRetryFilter(MAX_RETRIES, RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new GatewayTimeoutFilter());
        client.addFilter(new TerminatedHandshakeRetryFilter(MAX_RETRIES, RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new KnabFailureFilter());
        client.addFilter(new ConnectionTimeoutRetryFilter(MAX_RETRIES, RETRY_SLEEP_MILLISECONDS));
        client.setCookieSpec(CookieSpecs.STANDARD);
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final AgentConfiguration<KnabConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(KnabConfiguration.class);

        this.apiClient.applyConfiguration(
                agentConfiguration.getProviderSpecificConfiguration(),
                agentConfiguration.getRedirectUrl());
        this.client.setEidasProxy(configuration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new KnabAuthenticator(strongAuthenticationState, apiClient, storage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
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

    private TransactionalAccountRefreshController createTransactionalAccountRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                new KnabAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new KnabTransactionFetcher(apiClient))
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
