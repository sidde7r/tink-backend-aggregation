package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CREDIT_CARDS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration.ICSConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSCreditCardFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.filter.ICSRetryFilter;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.AccessExceededFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.BankServiceInternalErrorFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CREDIT_CARDS})
public final class ICSAgent extends NextGenerationAgent
        implements RefreshCreditCardAccountsExecutor {

    private final ICSApiClient apiClient;

    private final CreditCardRefreshController creditCardRefreshController;

    public ICSAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        configureHttpClient(client);

        final AgentConfiguration<ICSConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(ICSConfiguration.class);
        final ICSConfiguration icsConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();

        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());

        final String customerIpAddress = request.isManual() ? userIp : "";
        apiClient =
                new ICSApiClient(
                        client,
                        sessionStorage,
                        persistentStorage,
                        agentConfiguration.getRedirectUrl(),
                        icsConfiguration,
                        customerIpAddress);

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    private void configureHttpClient(TinkHttpClient client) {
        client.addFilter(new BankServiceInternalErrorFilter());
        client.addFilter(
                new ICSRetryFilter(HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new AccessExceededFilter(this.provider != null ? this.provider.getName() : null));
        client.addFilter(new TimeoutFilter());
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

    private CreditCardRefreshController constructCreditCardRefreshController() {
        return new CreditCardRefreshController(
                metricRefreshController,
                updateController,
                new ICSAccountFetcher(apiClient),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new ICSCreditCardFetcher(apiClient, persistentStorage))
                                .build(),
                        null));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new ICSSessionHandler();
    }
}
