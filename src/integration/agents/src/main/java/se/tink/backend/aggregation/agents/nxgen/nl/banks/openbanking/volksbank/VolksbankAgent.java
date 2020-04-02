package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.HttpClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.VolksbankConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.ConsentFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.VolksbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter.BankErrorResponseFilter;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.filter.VolksbankRetryFilter;
import se.tink.backend.aggregation.agents.progressive.ProgressiveAuthAgent;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.ProductionAgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.ProgressiveAuthController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationRequest;
import se.tink.backend.aggregation.nxgen.controllers.authentication.SteppableAuthenticationResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.progressive.AutoAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationProgressiveController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.retry.TimeoutRetryFilter;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class VolksbankAgent
        extends SubsequentGenerationAgent<AutoAuthenticationProgressiveController>
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ProgressiveAuthAgent {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final AutoAuthenticationProgressiveController progressiveAuthenticator;

    public VolksbankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(
                ProductionAgentComponentProvider.create(
                        request, context, agentsServiceConfiguration.getSignatureKeyPair()));

        final String[] payload = request.getProvider().getPayload().split(" ");

        final String bankPath = payload[1];

        final VolksbankUrlFactory urlFactory = new VolksbankUrlFactory(Urls.HOST, bankPath);

        final VolksbankConfiguration volksbankConfiguration =
                getAgentConfigurationController()
                        .getAgentConfiguration(VolksbankConfiguration.class);

        final VolksbankApiClient volksbankApiClient = new VolksbankApiClient(client, urlFactory);

        final URL redirectUrl = new URL(volksbankConfiguration.getRedirectUrl());
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
                        persistentStorage, authenticator, credentials, strongAuthenticationState);
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
        // Prevent read timeouts
        client.setTimeout(HttpClient.READ_TIMEOUT_MILLISECONDS);
        client.addFilter(
                new TimeoutRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(
                new VolksbankRetryFilter(
                        HttpClient.MAX_RETRIES, HttpClient.RETRY_SLEEP_MILLISECONDS));
        client.addFilter(new BankErrorResponseFilter());
        client.getInternalClient();
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
