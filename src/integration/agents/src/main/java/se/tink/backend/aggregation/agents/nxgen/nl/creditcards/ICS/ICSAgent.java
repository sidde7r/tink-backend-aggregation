package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import static se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.HttpClient.MAX_RETRIES;
import static se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.HttpClient.RATE_LIMIT_MAX_RETRIES;
import static se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.HttpClient.RATE_LIMIT_RETRY_MS_MAX;
import static se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.HttpClient.RATE_LIMIT_RETRY_MS_MIN;
import static se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.HttpClient.RETRY_SLEEP_MILLISECONDS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration.ICSConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter.ICSApiClientConfigurator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter.ICSRateLimitFilterProperties;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter.ICSRetryFilterProperties;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

@AgentCapabilities({CREDIT_CARDS})
public final class ICSAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final ICSApiClient apiClient;

    private final CreditCardRefreshController creditCardRefreshController;

    @Inject
    public ICSAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
        configureHttpClient(client);

        final AgentConfiguration<ICSConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(ICSConfiguration.class);
        final ICSConfiguration icsConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();

        final String customerIpAddress =
                request.getUserAvailability().isUserPresent() ? userIp : "";
        apiClient =
                new ICSApiClient(
                        client,
                        sessionStorage,
                        persistentStorage,
                        agentConfiguration.getRedirectUrl(),
                        icsConfiguration,
                        customerIpAddress,
                        componentProvider);

        final LocalDateTimeSource localDateTimeSource = componentProvider.getLocalDateTimeSource();
        creditCardRefreshController = constructCreditCardRefreshController(localDateTimeSource);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setEidasProxy(configuration.getEidasProxy());
    }

    private void configureHttpClient(TinkHttpClient client) {
        new ICSApiClientConfigurator()
                .applyFilters(
                        client,
                        new ICSRetryFilterProperties(MAX_RETRIES, RETRY_SLEEP_MILLISECONDS),
                        new ICSRateLimitFilterProperties(
                                RATE_LIMIT_RETRY_MS_MIN,
                                RATE_LIMIT_RETRY_MS_MAX,
                                RATE_LIMIT_MAX_RETRIES),
                        provider.getName());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new ICSOAuthAuthenticator(apiClient, sessionStorage, persistentStorage),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
    }

    @Override
    public FetchAccountsResponse fetchCreditCardAccounts() {
        return creditCardRefreshController.fetchCreditCardAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCreditCardTransactions() {
        return creditCardRefreshController.fetchCreditCardTransactions();
    }

    private CreditCardRefreshController constructCreditCardRefreshController(
            LocalDateTimeSource localDateTimeSource) {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new ICSAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new ICSCreditCardFetcher(apiClient, persistentStorage))
                                .setLocalDateTimeSource(localDateTimeSource)
                                .build(),
                        null));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new ICSSessionHandler();
    }
}
