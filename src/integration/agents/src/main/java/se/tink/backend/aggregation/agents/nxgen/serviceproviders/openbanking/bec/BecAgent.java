package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec;

import static se.tink.backend.aggregation.agents.agentcapabilities.Capability.CHECKING_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.agent.sdk.operation.Provider;
import se.tink.agent.sdk.operation.User;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.BecAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.authenticator.BecController;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.configuration.BecConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.BecTransactionalAccountFetcher;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.configuration.eidas.proxy.EidasProxyConfiguration;
import se.tink.backend.aggregation.nxgen.agents.NextGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.http.filter.filters.ServiceUnavailableBankServiceErrorFilter;

@AgentCapabilities({CHECKING_ACCOUNTS})
public final class BecAgent extends NextGenerationAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final BecApiClient apiClient;
    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    @Inject
    public BecAgent(
            AgentComponentProvider componentProvider,
            AgentsServiceConfiguration agentsServiceConfiguration) {
        super(componentProvider);

        apiClient =
                new BecApiClient(
                        client,
                        persistentStorage,
                        getApiConfiguration(
                                componentProvider.getUser(), componentProvider.getProvider()));
        transactionalAccountRefreshController = getTransactionalAccountRefreshController();

        super.setConfiguration(agentsServiceConfiguration);

        apiClient.setConfiguration(
                getAgentConfiguration(), agentsServiceConfiguration, this.getEidasIdentity());

        final EidasProxyConfiguration eidasProxyConfiguration = configuration.getEidasProxy();

        client.addFilter(new ServiceUnavailableBankServiceErrorFilter());
        client.setEidasProxy(eidasProxyConfiguration);
    }

    private BecApiConfiguration getApiConfiguration(User user, Provider provider) {
        String url = provider.getPayload().split(",")[1];

        return BecApiConfiguration.builder()
                .url(url)
                .userIp(user.getIpAddress())
                .isUserPresent(user.isPresent())
                .build();
    }

    private AgentConfiguration<BecConfiguration> getAgentConfiguration() {
        return getAgentConfigurationController().getAgentConfiguration(BecConfiguration.class);
    }

    @Override
    protected Authenticator constructAuthenticator() {
        final BecController becController =
                new BecController(
                        supplementalInformationHelper,
                        persistentStorage,
                        new BecAuthenticator(apiClient),
                        strongAuthenticationState);

        return new AutoAuthenticationController(
                request,
                systemUpdater,
                new ThirdPartyAppAuthenticationController<>(
                        becController, supplementalInformationHelper),
                becController);
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
        BecTransactionalAccountFetcher accountFetcher =
                new BecTransactionalAccountFetcher(apiClient);

        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                accountFetcher,
                new TransactionFetcherController<>(
                        transactionPaginationHelper,
                        new TransactionDatePaginationController.Builder<>(accountFetcher)
                                .setConsecutiveEmptyPagesLimit(6)
                                .build()));
    }

    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }
}
