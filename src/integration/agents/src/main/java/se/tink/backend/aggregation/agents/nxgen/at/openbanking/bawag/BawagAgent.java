package se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.BawagConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.BawagAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.authenticator.BawagAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.configuration.BawagConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.executor.payment.BawagPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.fetcher.transactionalaccount.BawagTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.bawag.util.BawagUtils;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class BawagAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final String clientName;
    private final BawagApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public BawagAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new BawagApiClient(client, persistentStorage);
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);

        BawagConfiguration clientConfiguration = getClientConfiguration();
        apiClient.setConfiguration(clientConfiguration);

        client.setSslClientCertificate(
                BawagUtils.readFile(clientConfiguration.getKeystorePath()),
                clientConfiguration.getKeystorePassword());
    }

    protected BawagConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        BawagConstants.INTEGRATION_NAME, clientName, BawagConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final BawagAuthenticationController controller =
                new BawagAuthenticationController(
                        supplementalInformationHelper,
                        new BawagAuthenticator(
                                apiClient,
                                persistentStorage,
                                credentials.getField(CredentialKeys.IBAN)),
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
        final BawagTransactionalAccountFetcher accountFetcher =
                new BawagTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(accountFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        BawagPaymentExecutor bawagPaymentExecutor = new BawagPaymentExecutor(apiClient);
        return Optional.of(new PaymentController(bawagPaymentExecutor, bawagPaymentExecutor));
    }
}
