package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen;

import java.util.Optional;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.RaiffeisenAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.authenticator.RaiffeisenAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.configuration.RaiffeisenConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.executor.payment.RaiffeisenPaymentExecutor;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.RaiffeisenTransactionalAccountAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.RaiffeisenTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
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

/** Raiffeisen uses IBAN flow. */
public final class RaiffeisenAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final String clientName;
    private final RaiffeisenApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public RaiffeisenAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        apiClient = new RaiffeisenApiClient(client, persistentStorage, credentials, sessionStorage);
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        RaiffeisenConfiguration raiffeisenConfiguration = getClientConfiguration();
        apiClient.setConfiguration(raiffeisenConfiguration);
        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private RaiffeisenConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfigurationFromK8s(
                        RaiffeisenConstants.INTEGRATION_NAME,
                        clientName,
                        RaiffeisenConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {

        RaiffeisenAuthenticator authenticator = new RaiffeisenAuthenticator(apiClient);
        RaiffeisenAuthenticationController controller =
                new RaiffeisenAuthenticationController(
                        supplementalInformationHelper,
                        authenticator,
                        sessionStorage,
                        persistentStorage,
                        strongAuthenticationState);
        return new AutoAuthenticationController(
                request,
                context,
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
        final RaiffeisenTransactionalAccountAccountFetcher accountFetcher =
                new RaiffeisenTransactionalAccountAccountFetcher(apiClient);

        final RaiffeisenTransactionalAccountTransactionFetcher transactionFetcher =
                new RaiffeisenTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public Optional<PaymentController> constructPaymentController() {
        RaiffeisenPaymentExecutor raiffeisenPaymentExecutor =
                new RaiffeisenPaymentExecutor(apiClient);
        return Optional.of(
                new PaymentController(raiffeisenPaymentExecutor, raiffeisenPaymentExecutor));
    }
}
