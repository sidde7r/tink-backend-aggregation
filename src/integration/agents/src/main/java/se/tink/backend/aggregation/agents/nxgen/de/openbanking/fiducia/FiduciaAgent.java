package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import com.google.inject.Inject;
import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.configuration.FiduciaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.detail.FiduciaRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.FiduciaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.FiduciaTransactionalAccountFetcher;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.header.SignatureHeaderGenerator;

public final class FiduciaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final FiduciaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public FiduciaAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);

        String serverUrl = request.getProvider().getPayload();
        FiduciaRequestBuilder fiduciaRequestBuilder =
                new FiduciaRequestBuilder(
                        client,
                        sessionStorage,
                        getClientConfiguration(),
                        createSignatureHeaderGenerator(qsealcSigner));

        this.apiClient = new FiduciaApiClient(persistentStorage, serverUrl, fiduciaRequestBuilder);

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private SignatureHeaderGenerator createSignatureHeaderGenerator(QsealcSigner qsealcSigner) {
        return new SignatureHeaderGenerator(
                FiduciaConstants.SIGNATURE_HEADER,
                FiduciaConstants.HEADERS_TO_SIGN,
                getClientConfiguration().getKeyId(),
                qsealcSigner);
    }

    protected FiduciaConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(FiduciaConfiguration.class)
                .validateConfig();
    }

    @Override
    protected Authenticator constructAuthenticator() {
        FiduciaAuthenticator fiduciaAuthenticator =
                new FiduciaAuthenticator(
                        apiClient,
                        persistentStorage,
                        sessionStorage,
                        supplementalInformationHelper);

        return new AutoAuthenticationController(
                request, context, fiduciaAuthenticator, fiduciaAuthenticator);
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
        final FiduciaTransactionalAccountFetcher accountFetcher =
                new FiduciaTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(accountFetcher)));
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final FiduciaPaymentExecutor fiduciaPaymentExecutor =
                new FiduciaPaymentExecutor(
                        apiClient,
                        getClientConfiguration(),
                        credentials.getField(CredentialKeys.PSU_ID),
                        credentials.getField(CredentialKeys.PASSWORD));

        return Optional.of(new PaymentController(fiduciaPaymentExecutor, fiduciaPaymentExecutor));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
