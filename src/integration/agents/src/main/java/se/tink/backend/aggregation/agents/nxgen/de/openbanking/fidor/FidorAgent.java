package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.authenticator.FidorAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.configuration.FidorConfiguration;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.FidorAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.FidorTransactionFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionPagePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class FidorAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final FidorApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public FidorAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        apiClient = new FidorApiClient(client, persistentStorage);

        transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    @Override
    public void setConfiguration(final AgentsServiceConfiguration agentsServiceConfiguration) {
        super.setConfiguration(agentsServiceConfiguration);
        apiClient.setConfiguration(getAgentConfiguration());
        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    private AgentConfiguration<FidorConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(FidorConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        FidorAuthenticator fidorAuthenticator =
                new FidorAuthenticator(
                        supplementalInformationHelper,
                        strongAuthenticationState,
                        apiClient,
                        persistentStorage,
                        getAgentConfiguration().getProviderSpecificConfiguration(),
                        credentials);
        return new AutoAuthenticationController(
                request,
                context,
                new ThirdPartyAppAuthenticationController<>(
                        fidorAuthenticator, supplementalInformationHelper),
                fidorAuthenticator);
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

        final FidorAccountFetcher accountFetcher = new FidorAccountFetcher(apiClient);
        final FidorTransactionFetcher transactionFetcher = new FidorTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionPagePaginationController<>(transactionFetcher, 1)));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
