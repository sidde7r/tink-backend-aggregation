package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.authenticator.SantanderAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.configuration.SantanderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.SantanderPaymentExecutorSelector;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.fetcher.transactionalaccount.SantanderTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.payment.PaymentController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class SantanderAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final SantanderApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public SantanderAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new SantanderApiClient(client, persistentStorage);
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        apiClient.setConfiguration(getAgentConfiguration());
        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private AgentConfiguration<SantanderConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfiguration(SantanderConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        return new SantanderAuthenticator(
                apiClient,
                persistentStorage,
                getAgentConfiguration().getProviderSpecificConfiguration(),
                credentials.getField(SantanderConstants.CredentialKeys.IBAN));
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
        final SantanderTransactionalAccountFetcher accountFetcher =
                new SantanderTransactionalAccountFetcher(apiClient);

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
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        final SantanderPaymentExecutorSelector paymentExecutorSelector =
                new SantanderPaymentExecutorSelector(apiClient);

        return Optional.of(new PaymentController(paymentExecutorSelector, paymentExecutorSelector));
    }
}
