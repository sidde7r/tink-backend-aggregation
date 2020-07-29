package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import com.google.common.base.Strings;
import com.google.inject.Inject;
import java.security.cert.CertificateException;
import java.util.Objects;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaSignatureHeaderGenerator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.detail.FiduciaRequestBuilder;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.FiduciaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.FiduciaTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agents.EmptyConfiguration;
import se.tink.backend.aggregation.configuration.agents.utils.CertificateUtils;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.eidassigner.module.QSealcSignerModuleRSASHA256;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentDependencyModules(modules = QSealcSignerModuleRSASHA256.class)
@Slf4j
public final class FiduciaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final FiduciaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    private String tppOrganizationIdentifier;
    private String qsealcDerBase64;

    @Inject
    public FiduciaAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        String serverUrl = request.getProvider().getPayload();
        extractDataFromCertificates();
        FiduciaRequestBuilder fiduciaRequestBuilder =
                new FiduciaRequestBuilder(
                        client,
                        sessionStorage,
                        tppOrganizationIdentifier,
                        qsealcDerBase64,
                        getAgentConfiguration().getRedirectUrl(),
                        createSignatureHeaderGenerator(qsealcSigner));

        this.apiClient = new FiduciaApiClient(persistentStorage, serverUrl, fiduciaRequestBuilder);

        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private void extractDataFromCertificates() {
        try {
            tppOrganizationIdentifier =
                    CertificateUtils.getOrganizationIdentifier(getAgentConfiguration().getQsealc());
            qsealcDerBase64 =
                    CertificateUtils.getDerEncodedCertFromBase64EncodedCertificate(
                            getAgentConfiguration().getQsealc());
        } catch (CertificateException e) {
            log.error("Failed to extract organizationId or Qsealc from agent configuration", e);
        }
    }

    private FiduciaSignatureHeaderGenerator createSignatureHeaderGenerator(
            QsealcSigner qsealcSigner) {
        return new FiduciaSignatureHeaderGenerator(
                FiduciaConstants.SIGNATURE_HEADER, FiduciaConstants.HEADERS_TO_SIGN, qsealcSigner);
    }

    protected AgentConfiguration<EmptyConfiguration> getAgentConfiguration() {
        final AgentConfiguration<EmptyConfiguration> agentConfiguration =
                getAgentConfigurationController().getAgentConfiguration(EmptyConfiguration.class);
        Objects.requireNonNull(Strings.emptyToNull(agentConfiguration.getRedirectUrl()));
        return agentConfiguration;
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
                        qsealcDerBase64,
                        credentials.getField(CredentialKeys.PSU_ID),
                        credentials.getField(CredentialKeys.PASSWORD));

        return Optional.of(new PaymentController(fiduciaPaymentExecutor, fiduciaPaymentExecutor));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
