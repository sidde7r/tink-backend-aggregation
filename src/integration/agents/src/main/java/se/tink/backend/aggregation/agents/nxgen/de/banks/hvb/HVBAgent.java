package se.tink.backend.aggregation.agents.nxgen.de.banks.hvb;

import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.CHECKING_ACCOUNTS;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.IDENTITY_DATA;
import static se.tink.backend.aggregation.client.provider_configuration.rpc.Capability.SAVINGS_ACCOUNTS;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshIdentityDataExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.authenticator.HVBAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.AccountsFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.TransactionsFetcher;
import se.tink.backend.aggregation.agents.nxgen.de.banks.hvb.fetcher.UserDataFetcher;
import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcherController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page.TransactionKeyPaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.scaffold.ModuleDependenciesRegistry;

@AgentCapabilities({CHECKING_ACCOUNTS, SAVINGS_ACCOUNTS, IDENTITY_DATA})
public final class HVBAgent extends SubsequentGenerationAgent<HVBAuthenticator>
        implements RefreshCheckingAccountsExecutor,
                RefreshSavingsAccountsExecutor,
                RefreshIdentityDataExecutor {

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;
    private final ModuleDependenciesRegistry dependencyRegistry;

    @Inject
    public HVBAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);

        dependencyRegistry =
                initializeAgentDependencies(
                        new HVBModuleDependenciesRegistration(), componentProvider);
        transactionalAccountRefreshController = constructTransactionalAccountRefreshController();
        setupHttpClient();
    }

    private ModuleDependenciesRegistry initializeAgentDependencies(
            HVBModuleDependenciesRegistration moduleDependenciesRegistration,
            AgentComponentProvider componentProvider) {
        moduleDependenciesRegistration.registerExternalDependencies(
                client, sessionStorage, persistentStorage, componentProvider);
        moduleDependenciesRegistration.registerInternalModuleDependencies();
        return moduleDependenciesRegistration.createModuleDependenciesRegistry();
    }

    private void setupHttpClient() {
        client.setFollowRedirects(false);
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

    private TransactionalAccountRefreshController constructTransactionalAccountRefreshController() {
        return new TransactionalAccountRefreshController(
                metricRefreshController,
                updateController,
                dependencyRegistry.getBean(AccountsFetcher.class),
                new TransactionFetcherController<>(
                        this.transactionPaginationHelper,
                        new TransactionKeyPaginationController<>(
                                dependencyRegistry.getBean(TransactionsFetcher.class))));
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return SessionHandler.alwaysFail();
    }

    @Override
    public HVBAuthenticator getAuthenticator() {
        return dependencyRegistry.getBean(HVBAuthenticator.class);
    }

    @Override
    public boolean login() throws AuthenticationException, AuthorizationException {
        getAuthenticator().authenticate(credentials);
        return true;
    }

    @Override
    public FetchIdentityDataResponse fetchIdentityData() {
        return dependencyRegistry.getBean(UserDataFetcher.class).fetchIdentityData();
    }
}
