package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.RegioBankConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.ConsentFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.VolksbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter.BankErrorResponseFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter.VolksbankRetryFilter;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.progressive.AutoAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.progressive.ThirdPartyAppAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.ProgressiveAuthController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class VolksbankAgent
        extends SubsequentGenerationAgent<AutoAuthenticationProgressiveController>
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final AutoAuthenticationProgressiveController progressiveAuthenticator;
    private final String bankPath;

    public VolksbankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(
                ProductionAgentComponentProvider.create(
                        request, context, agentsServiceConfiguration.getSignatureKeyPair()));

        final String[] payload = request.getProvider().getPayload().split(" ");

        bankPath = payload[1];

        final VolksbankUrlFactory urlFactory = new VolksbankUrlFactory(Urls.HOST, bankPath);

        final AgentConfiguration<VolksbankConfiguration> agentConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(VolksbankConfiguration.class);
        final VolksbankConfiguration volksbankConfiguration =
                agentConfiguration.getProviderSpecificConfiguration();
        final VolksbankApiClient volksbankApiClient = new VolksbankApiClient(client, urlFactory);

        final URL redirectUrl = new URL(agentConfiguration.getRedirectUrl());
        final String clientId = volksbankConfiguration.getClientId();

        final ConsentFetcher consentFetcher =
                new ConsentFetcher(volksbankApiClient, persistentStorage, redirectUrl, clientId);

        final EidasProxyConfiguration eidasProxyConfiguration =
                agentsServiceConfiguration.getEidasProxy();

        client.setEidasProxy(eidasProxyConfiguration);

        configureHttpClient(client);

        transactionalAccountRefreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new VolksbankTransactionalAccountFetcher(
                                volksbankApiClient, consentFetcher, persistentStorage),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionDatePaginationController<>(
                                        new VolksbankTransactionFetcher(
                                                volksbankApiClient,
                                                consentFetcher,
                                                persistentStorage))));

        final String clientSecret = volksbankConfiguration.getClientSecret();

        VolksbankAuthenticator authenticator =
                new VolksbankAuthenticator(
                        volksbankApiClient,
                        persistentStorage,
                        redirectUrl,
                        urlFactory,
                        consentFetcher,
                        clientId,
                        clientSecret);

        OAuth2AuthenticationProgressiveController oAuth2AuthenticationController =
                new OAuth2AuthenticationProgressiveController(
                        persistentStorage,
                        authenticator,
                        credentials,
                        strongAuthenticationState,
                        request);
        final AutoAuthenticationProgressiveController autoAuthenticationController =
                new AutoAuthenticationProgressiveController(
                        request,
                        context,
                        new ThirdPartyAppAuthenticationProgressiveController(
                                oAuth2AuthenticationController),
                        oAuth2AuthenticationController);

        progressiveAuthenticator = autoAuthenticationController;
    }

    private void configureHttpClient(TinkHttpClient client) {
        // Temporarily extracted specific config for RegioBank to alter and monitor the filter
        // effectiveness without affecting all Volksbank providers.
        if (RegioBankConstants.REGIOBANK.equalsIgnoreCase(bankPath)) {
            configureHttpClientForRegiobank(client);
        } else {
            configureHttpClientForVolksbank(client);
        }

        client.addFilter(new BankErrorResponseFilter());
        client.getInternalClient();
    }

    private void configureHttpClientForRegiobank(TinkHttpClient client) {
        client.addFilter(new TimeoutFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        RegioBankConstants.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new VolksbankRetryFilter(
                        RegioBankConstants.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));

    }

    private void configureHttpClientForVolksbank(TinkHttpClient client) {
        // Prevent read timeouts
        client.setTimeout(HttpClient.READ_TIMEOUT_MILLISECONDS);
        client.addFilter(new TimeoutFilter());
        client.addFilter(
                new TimeoutRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new VolksbankRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
    }

    @Override
    public boolean login() throws Exception {
        throw new AssertionError(); // ProgressiveAuthAgent::login should always be used
    }

    @Override
    public SteppableAuthenticationResponse login(final SteppableAuthenticationRequest request)
            throws AuthenticationException, AuthorizationException {
        return ProgressiveAuthController.of(progressiveAuthenticator, credentials).login(request);
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

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public AutoAuthenticationProgressiveController getAuthenticator() {
        return progressiveAuthenticator;
    }
}
