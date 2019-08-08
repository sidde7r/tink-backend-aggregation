package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS;

import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCreditCardAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.ICSOAuthAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.configuration.ICSConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.ICSCreditCardFetcher;
import se.tink.backend.aggregation.agents.utils.encoding.EncodingUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.creditcard.CreditCardRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class ICSAgent extends NextGenerationAgent implements RefreshCreditCardAccountsExecutor {

    private final ICSApiClient apiClient;
    private final String clientName;
    private final String redirectUri;

    private final CreditCardRefreshController creditCardRefreshController;

    public ICSAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        configureHttpClient(client);
        clientName = request.getProvider().getPayload().split(" ")[0];
        redirectUri = request.getProvider().getPayload().split(" ")[1];
        apiClient = new ICSApiClient(client, sessionStorage, persistentStorage, redirectUri);

        creditCardRefreshController = constructCreditCardRefreshController();
    }

    protected void configureHttpClient(TinkHttpClient client) {
        client.disableSignatureRequestHeader();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        final ICSConfiguration icsConfiguration = getClientConfiguration();

        client.setSslClientCertificate(
                EncodingUtils.decodeBase64String(icsConfiguration.getClientSSLCertificate()), "");
        client.trustRootCaCertificate(
                EncodingUtils.decodeBase64String(icsConfiguration.getRootCACertificate()),
                icsConfiguration.getRootCAPassword());

        apiClient.setConfiguration(icsConfiguration);
    }

    public ICSConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        ICSConstants.INTEGRATION_NAME, clientName, ICSConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new ICSOAuthAuthenticator(apiClient, sessionStorage),
                        configuration.getCallbackJwtSignatureKeyPair(),
                        request);

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
                        new TransactionPagePaginationController<>(
                                new ICSCreditCardFetcher(apiClient), 0),
                        null));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new ICSSessionHandler();
    }
}
