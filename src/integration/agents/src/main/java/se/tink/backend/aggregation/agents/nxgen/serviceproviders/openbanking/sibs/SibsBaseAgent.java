package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.SibsConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.authenticator.SibsRedirectAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.configuration.SibsConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.SibsPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategy;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.executor.payment.sign.SignPaymentStrategyFactory;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.SibsTransactionalAccountAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.SibsTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.eidassigner.EidasIdentity;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public abstract class SibsBaseAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final String clientName;
    protected final SibsBaseApiClient apiClient;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SibsBaseAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new SibsBaseApiClient(client, persistentStorage, request.isManual());
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
    }

    protected abstract String getIntegrationName();

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        apiClient.setConfiguration(getClientConfiguration(), configuration.getEidasProxy());
        client.setMessageSignInterceptor(
                new SibsMessageSignInterceptor(
                        getClientConfiguration(),
                        configuration.getEidasProxy(),
                        new EidasIdentity(
                                context.getClusterId(), context.getAppId(), this.getAgentClass())));
    }

    protected SibsConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(getIntegrationName(), clientName, SibsConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {

        final SibsRedirectAuthenticationController controller =
                new SibsRedirectAuthenticationController(
                        supplementalInformationHelper,
                        new SibsAuthenticator(apiClient, credentials),
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        final SibsTransactionalAccountAccountFetcher accountFetcher =
                new SibsTransactionalAccountAccountFetcher(apiClient);
        final SibsTransactionalAccountTransactionFetcher transactionFetcher =
                new SibsTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        SignPaymentStrategy signPaymentStrategy =
                SignPaymentStrategyFactory.buildSignPaymentRedirectStrategy(
                        apiClient, supplementalInformationHelper);
        SibsPaymentExecutor sibsPaymentExecutor =
                new SibsPaymentExecutor(apiClient, signPaymentStrategy);
        return Optional.of(new PaymentController(sibsPaymentExecutor, sibsPaymentExecutor));
    }
}
