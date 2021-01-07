package se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.VolksbankAuthenticationController;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.authenticator.VolksbankAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.configuration.VolksbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.volksbank.fetcher.transactionalaccount.VolksbankTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.configuration.signaturekeypair.SignatureKeyPair;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities(generateFromImplementedExecutors = true)
public final class VolksbankAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final String clientName;
    private final VolksbankApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public VolksbankAgent(
            CredentialsRequest request, AgentContext context, SignatureKeyPair signatureKeyPair) {
        super(request, context, signatureKeyPair);

        apiClient = new VolksbankApiClient(client, persistentStorage, sessionStorage, credentials);
        clientName = request.getProvider().getPayload();

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        apiClient.setConfiguration(getClientConfiguration());
    }

    protected VolksbankConfiguration getClientConfiguration() {
        return getAgentConfigurationController()
                .getAgentConfigurationFromK8s(
                        VolksbankConstants.INTEGRATION_NAME,
                        clientName,
                        VolksbankConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final VolksbankAuthenticationController controller =
                new VolksbankAuthenticationController(
                        supplementalInformationHelper,
                        new VolksbankAuthenticator(apiClient),
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

        final VolksbankTransactionalAccountFetcher accountFetcher =
                new VolksbankTransactionalAccountFetcher(apiClient);
        final VolksbankTransactionalAccountTransactionFetcher transactionFetcher =
                new VolksbankTransactionalAccountTransactionFetcher(apiClient);

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
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
