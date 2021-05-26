package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.FiduciaConstants.CredentialKeys;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.authenticator.FiduciaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.executor.payment.FiduciaPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.FiduciaTransactionalAccountFetcher;
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
@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class FiduciaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final FiduciaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public FiduciaAgent(AgentComponentProvider componentProvider, QsealcSigner qsealcSigner) {
        super(componentProvider);
        String serverUrl = request.getProvider().getPayload();

        this.apiClient =
                new FiduciaApiClient(
                        client,
                        persistentStorage,
                        request.getUserAvailability().isUserPresent()
                                ? request.getUserAvailability().getOriginatingUserIp()
                                : null,
                        serverUrl);
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
        client.addFilter(
                new FiduciaSigningFilter(
                        qsealcSigner,
                        getAgentConfigurationController()
                                .getAgentConfiguration(FiduciaConfiguration.class)));
    }

    @Override
    protected Authenticator constructAuthenticator() {
        FiduciaAuthenticator fiduciaAuthenticator =
                new FiduciaAuthenticator(
                        credentials,
                        apiClient,
                        persistentStorage,
                        supplementalInformationHelper,
                        catalog);

        return new AutoAuthenticationController(
                request, context, fiduciaAuthenticator, fiduciaAuthenticator);
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
    public Optional<PaymentController> constructPaymentController() {
        final FiduciaPaymentExecutor fiduciaPaymentExecutor =
                new FiduciaPaymentExecutor(
                        apiClient,
                        credentials.getField(CredentialKeys.PSU_ID),
                        credentials.getField(CredentialKeys.PASSWORD));

        return Optional.of(new PaymentController(fiduciaPaymentExecutor, fiduciaPaymentExecutor));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
