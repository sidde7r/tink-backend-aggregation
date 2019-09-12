package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.ManualOrAutoAuth;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.authenticator.IngBaseAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.configuration.IngBaseConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseAccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.fetcher.IngBaseTransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ingbase.session.IngSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class IngBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                ManualOrAutoAuth {

    private final String clientName;
    protected final IngBaseApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private AutoAuthenticationController authenticator;

    public IngBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        /*
            ING in their documentation use country code in lowercase, however their API treat
            lowercase as wrong country code and returns error that it's malformed
        */
        final String marketInUppercase = request.getProvider().getMarket().toUpperCase();
        apiClient =
                new IngBaseApiClient(
                        client, persistentStorage, marketInUppercase, request.isManual());
        clientName = request.getProvider().getPayload();
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        final IngBaseConfiguration ingBaseConfiguration = getClientConfiguration();

        EidasIdentity eidasIdentity =
                new EidasIdentity(context.getClusterId(), context.getAppId(), this.getAgentClass());

        apiClient.setConfiguration(
                ingBaseConfiguration, configuration.getEidasProxy(), eidasIdentity);
        client.setEidasProxy(
                configuration.getEidasProxy(), ingBaseConfiguration.getCertificateId());
    }

    protected IngBaseConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        getIntegrationName(), clientName, IngBaseConfiguration.class)
                .orElseThrow(
                        () ->
                                new IllegalStateException(
                                        IngBaseConstants.ErrorMessages.MISSING_CONFIGURATION));
    }

    protected abstract String getIntegrationName();

    @Override
    protected Authenticator constructAuthenticator() {
        final IngBaseAuthenticator ingBaseAuthenticator =
                new IngBaseAuthenticator(apiClient, persistentStorage);
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        ingBaseAuthenticator,
                        credentials,
                        strongAuthenticationState);
        authenticator =
                new AutoAuthenticationController(
                        request,
                        context,
                        new ThirdPartyAppAuthenticationController<>(
                                oAuth2AuthenticationController, supplementalInformationHelper),
                        oAuth2AuthenticationController);
        return authenticator;
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
                new IngBaseAccountsFetcher(
                        apiClient, request.getProvider().getCurrency().toUpperCase()),
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(
                                new IngBaseTransactionsFetcher(apiClient))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new IngSessionHandler();
    }

    @Override
    public boolean isManualAuthentication(Credentials credentials) {
        if (authenticator != null) {
            return authenticator.isManualAuthentication(credentials);
        }
        return false;
    }
}
