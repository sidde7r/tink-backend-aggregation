package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca;

import static java.util.Objects.requireNonNull;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.contexts.agent.AgentContext;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.ChebancaAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.configuration.ChebancaConfiguration;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.ChebancaConsentManualApproveController;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.ChebancaTransactionFetcher;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.fetcher.transactionalaccount.ChebancaTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2AuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.libraries.credentials.service.CredentialsRequest;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS})
public final class ChebancaAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final ChebancaApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    public ChebancaAgent(
            CredentialsRequest request,
            AgentContext context,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(request, context, agentsServiceConfiguration.getSignatureKeyPair());
        requireNonNull(request);
        requireNonNull(context);
        requireNonNull(agentsServiceConfiguration);
        apiClient =
                new ChebancaApiClient(
                        client,
                        persistentStorage,
                        strongAuthenticationState,
                        getAgentConfiguration(),
                        agentsServiceConfiguration,
                        this.getEidasIdentity());

        client.setFollowRedirects(false);

        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());
        this.transactionalAccountRefreshController = getTransactionalAccountRefreshController();
    }

    protected AgentConfiguration<ChebancaConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(ChebancaConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final OAuth2AuthenticationController controller =
                new OAuth2AuthenticationController(
                        persistentStorage,
                        supplementalInformationHelper,
                        new ChebancaAuthenticator(
                                apiClient, getAgentConfiguration(), strongAuthenticationState),
                        credentials,
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
        final ChebancaTransactionalAccountFetcher accountFetcher =
                new ChebancaTransactionalAccountFetcher(
                        apiClient,
                        new ChebancaConsentManualApproveController(
                                supplementalInformationHelper, strongAuthenticationState));

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(
                                        new ChebancaTransactionFetcher(apiClient))
                                .build()));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
