package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator.AuthorizePointsEnum;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.authenticator.CreditAgricoleAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.configuration.CreditAgricoleConfiguration;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.CreditAgricoleTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.CreditAgricolePaymentController;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.payment.CreditAgricolePaymentExecutor;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class CreditAgricoleAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final String clientName;
    private final CreditAgricoleApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private CreditAgricoleConfiguration creditAgricoleConfiguration;

    public CreditAgricoleAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair, true);

//        AuthorizePointsEnum bank = AuthorizePointsEnum.PARIS_ET_D_ILE_DE_FRANCE; //johanes
        AuthorizePointsEnum bank = AuthorizePointsEnum.L_ANJOU_ET_DU_MAINE;
//        AuthorizePointsEnum bank = AuthorizePointsEnum.SAVOIE; // francois
        persistentStorage.put(CreditAgricoleConstants.StorageKeys.BANK_URL, bank);


        apiClient = new CreditAgricoleApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        client.setDebugOutput(true);
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        creditAgricoleConfiguration = getClientConfiguration();
        apiClient.setConfiguration(creditAgricoleConfiguration, configuration.getEidasProxy());
        client.setMessageSignInterceptor(
            new CreditAgricoleMessageSignInterceptor(
                creditAgricoleConfiguration,
                configuration.getEidasProxy(),
                new EidasIdentity(
                    context.getClusterId(), context.getAppId(), this.getAgentClass())));

        client.setEidasProxy(configuration.getEidasProxy());
    }

    protected CreditAgricoleConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfigurationFromK8s(
                        CreditAgricoleConstants.INTEGRATION_NAME,
                        clientName,
                        CreditAgricoleConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new CreditAgricoleAuthenticator(
                                apiClient, persistentStorage, creditAgricoleConfiguration),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
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

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final CreditAgricoleTransactionalAccountFetcher accountFetcher =
                new CreditAgricoleTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController, updateController, accountFetcher, accountFetcher);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(
                new CreditAgricolePaymentController(
                        new CreditAgricolePaymentExecutor(
                                apiClient, sessionStorage, getClientConfiguration()),
                        supplementalInformationHelper,
                        sessionStorage,
                        strongAuthenticationState));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
