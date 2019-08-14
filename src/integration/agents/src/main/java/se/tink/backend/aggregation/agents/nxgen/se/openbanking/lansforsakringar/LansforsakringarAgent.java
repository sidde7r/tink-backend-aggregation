package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.LansforsakringarConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.authenticator.LansforsakringarAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.configuration.LansforsakringarConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.executor.payment.LansforsakringarPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.transactionalaccount.LansforsakringarTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.session.LansforsakringarSessionHandler;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class LansforsakringarAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final LansforsakringarApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private LansforsakringarConfiguration lansforsakringarConfiguration;

    public LansforsakringarAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new LansforsakringarApiClient(client, sessionStorage, credentials, persistentStorage);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        lansforsakringarConfiguration =
                configuration
                        .getIntegrations()
                        .getClientConfiguration(
                                LansforsakringarConstants.Market.INTEGRATION_NAME,
                                request.getProvider().getPayload(),
                                LansforsakringarConfiguration.class)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                ErrorMessages.MISSING_CONFIGURATION));

        client.setEidasProxy(
                configuration.getEidasProxy(), lansforsakringarConfiguration.getEidasQwac());
        apiClient.setConfiguration(lansforsakringarConfiguration);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        LansforsakringarAuthenticator lansforsakringarAuthenticator =
                new LansforsakringarAuthenticator(
                        apiClient,
                        sessionStorage,
                        lansforsakringarConfiguration,
                        persistentStorage);
        OAuth2AuthenticationController oAuth2AuthenticationController =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        lansforsakringarAuthenticator,
                        configuration.getCallbackJwtSignatureKeyPair(),
                        request);

        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        oAuth2AuthenticationController, supplementalInformationHelper),
                oAuth2AuthenticationController);
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        LansforsakringarPaymentExecutor lansforsakringarPaymentExecutor =
                new LansforsakringarPaymentExecutor(apiClient);

        return Optional.of(
                new PaymentController(
                        lansforsakringarPaymentExecutor, lansforsakringarPaymentExecutor));
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
        final LansforsakringarTransactionalAccountFetcher accountFetcher =
                new LansforsakringarTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return new LansforsakringarSessionHandler(apiClient);
    }
}
