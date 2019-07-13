package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.DnbConstants.ErrorMessages;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.DnbAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.authenticator.DnbAuthenticatorController;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.configuration.DnbConfiguration;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.executor.payment.DndPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.DnbAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.DnbTransactionFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class DnbAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final DnbApiClient apiClient;
    private final String clientName;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public DnbAgent(
            final CredentialsRequest request,
            final AgentContext context,
            final SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        final Credentials credentials = request.getCredentials();
        apiClient = new DnbApiClient(client, sessionStorage, credentials);
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(AgentsServiceConfiguration configuration) {
        super.setConfiguration(configuration);
        client.setSslClientCertificate(
                readFile(getClientConfiguration().getClientKeyStorePath()),
                getClientConfiguration().getClientKeyStorePassword());
        apiClient.setConfiguration(getClientConfiguration());
    }

    private DnbConfiguration getClientConfiguration() {
        return configuration
                .getIntegrations()
                .getClientConfiguration(
                        DnbConstants.INTEGRATION_NAME, clientName, DnbConfiguration.class)
                .orElseThrow(() -> new IllegalStateException(ErrorMessages.MISSING_CONFIGURATION));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final DnbAuthenticatorController controller =
                new DnbAuthenticatorController(
                        supplementalInformationHelper, new DnbAuthenticator(apiClient));

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
        final DnbAccountFetcher accountFetcher = new DnbAccountFetcher(apiClient);
        final DnbTransactionFetcher transactionFetcher = new DnbTransactionFetcher(apiClient);

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
    public Optional<PaymentController> constructPaymentController() {
        return Optional.of(new PaymentController(new DndPaymentExecutor(apiClient)));
    }

    private byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
