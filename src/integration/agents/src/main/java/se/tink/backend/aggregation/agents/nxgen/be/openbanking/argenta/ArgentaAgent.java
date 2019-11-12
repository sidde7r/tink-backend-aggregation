package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta;

import java.util.Optional;
import se.tink.backend.aggregation.agents.AgentContext;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.authenticator.ArgentaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.configuration.ArgentaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.ArgentaTransactionalAccountFetcher;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.ArgentaTransactionalAccountTransactionFetcher;
import se.tink.backend.aggregation.configuration.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.controllers.transfer.TransferController;
import se.tink.libraries.credentials.service.CredentialsRequest;

public final class ArgentaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final ArgentaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public ArgentaAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());

        String clientName = request.getProvider().getPayload();

        ArgentaConfiguration argentaConfiguration =
                getAgentConfigurationController().getAgentConfiguration(ArgentaConfiguration.class);
        super.setConfiguration(agentsServiceConfiguration);

        this.apiClient =
                new ArgentaApiClient(
                        client,
                        argentaConfiguration,
                        persistentStorage,
                        sessionStorage,
                        agentsServiceConfiguration,
                        getEidasIdentity());
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        this.client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new ArgentaAuthenticator(
                                credentials,
                                apiClient,
                                persistentStorage,
                                supplementalInformationHelper,
                                catalog),
                        credentials,
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        controller, supplementalInformationHelper),
                controller);
    }

    private TransactionalAccountRefreshController getTransactionalAccountRefreshController() {
        final ArgentaTransactionalAccountFetcher accountFetcher =
                new ArgentaTransactionalAccountFetcher(apiClient);

        final ArgentaTransactionalAccountTransactionFetcher transactionFetcher =
                new ArgentaTransactionalAccountTransactionFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController<>(transactionFetcher)));
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
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    protected Optional<TransferController> constructTransferController() {
        return Optional.empty();
    }
}
