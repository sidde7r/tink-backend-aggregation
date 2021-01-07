package se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.authenticator.TargobankSandboxAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.configuration.TargobankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.executor.payment.TargobankPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.TargobankAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.targobank.fetcher.TargobankTransactionFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class TargobankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor {

    private final TargobankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public TargobankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);
        apiClient = new TargobankApiClient(client, persistentStorage);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final TargobankAccountFetcher accountFetcher = new TargobankAccountFetcher(apiClient);

        final TargobankTransactionFetcher transactionFetcher =
                new TargobankTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(transactionFetcher)
                                .build()));
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        apiClient.setConfiguration(getAgentConfiguration());
        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private AgentConfiguration<TargobankConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(TargobankConfiguration.class);
    }

    /*ToDo Add Metrics when flow is done*/
    @Override
    protected Authenticator constructAuthenticator() {
        return new TargobankSandboxAuthenticator(apiClient, persistentStorage);
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }

    public FetchAccountsResponse fetchCheckingAccounts() {
        return transactionalAccountRefreshController.fetchCheckingAccounts();
    }

    @Override
    public FetchTransactionsResponse fetchCheckingTransactions() {
        return transactionalAccountRefreshController.fetchCheckingTransactions();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final TargobankPaymentExecutor targobankExecutor = new TargobankPaymentExecutor(apiClient);

        return Optional.of(new PaymentController(targobankExecutor, targobankExecutor));
    }
}
