package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.ConsentFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.authenticator.VolksbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class VolksbankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final VolksbankApiClient volksbankApiClient;
    private final VolksbankUrlFactory urlFactory;
    private final VolksbankConfiguration volksbankConfiguration;
    private final ConsentFetcher consentFetcher;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public VolksbankAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        final String[] payload = request.getProvider().getPayload().split(" ");

        final String clientName = payload[0];
        final String bankPath = payload[1];

        final boolean isSandbox = request.getProvider().getName().toLowerCase().contains("sandbox");

        this.urlFactory = new VolksbankUrlFactory(bankPath, isSandbox);

        volksbankApiClient = new VolksbankApiClient(client, urlFactory);

        consentFetcher = new ConsentFetcher(volksbankApiClient, persistentStorage, isSandbox);

        transactionalAccountRefreshController =
                new TransactionalAccountRefreshController(
                        metricRefreshController,
                        updateController,
                        new VolksbankTransactionalAccountFetcher(
                                volksbankApiClient, consentFetcher, persistentStorage),
                        new TransactionFetcherController<>(
                                this.transactionPaginationHelper,
                                new TransactionKeyPaginationController<>(
                                        new VolksbankTransactionFetcher(
                                                volksbankApiClient,
                                                consentFetcher,
                                                persistentStorage))));

        volksbankConfiguration =
                agentsServiceConfiguration
                        .getIntegrations()
                        .getClientConfiguration(
                                VolksbankConstants.Market.INTEGRATION_NAME,
                                clientName,
                                VolksbankConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Volksbank configuration missing."));

        volksbankApiClient.setConfiguration(volksbankConfiguration);

        final String certificateId =
                volksbankConfiguration.getAisConfiguration().getCertificateId();

        final EidasProxyConfiguration eidasProxyConfiguration =
                agentsServiceConfiguration.getEidasProxy();

        client.setEidasProxy(eidasProxyConfiguration, certificateId);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final URL redirectUrl = volksbankConfiguration.getAisConfiguration().getRedirectUrl();
        VolksbankAuthenticator authenticator =
                new VolksbankAuthenticator(
                        volksbankApiClient,
                        persistentStorage,
                        redirectUrl,
                        urlFactory,
                        consentFetcher);

        OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        authenticator,
                        credentials);
        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
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
}
