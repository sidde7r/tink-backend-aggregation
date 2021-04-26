package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.FetchAccountsResponse;
import se.tink.backend.aggregation.agents.FetchTransactionsResponse;
import se.tink.backend.aggregation.agents.RefreshCheckingAccountsExecutor;
import se.tink.backend.aggregation.agents.RefreshSavingsAccountsExecutor;
import se.tink.backend.aggregation.agents.agentcapabilities.AgentCapabilities;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.fetcher.TransactionalAccountRefreshControllerFactory;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAbstractMultiStepsAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.client.provider_configuration.rpc.Capability;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transactionalaccount.TransactionalAccountRefreshController;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentCapabilities({Capability.CHECKING_ACCOUNTS, Capability.SAVINGS_ACCOUNTS})
@AgentDependencyModules(modules = MetroModule.class)
public class MetroAgent extends AgentPlatformAgent
        implements RefreshCheckingAccountsExecutor, RefreshSavingsAccountsExecutor {

    private final AgentAbstractMultiStepsAuthenticationProcess authenticationProcessFacade;

    private final TransactionalAccountRefreshController transactionalAccountRefreshController;

    private final SessionHandler sessionHandler;

    @Inject
    protected MetroAgent(
            AgentComponentProvider componentProvider,
            AgentAbstractMultiStepsAuthenticationProcess authenticationProcessFacade,
            SessionHandler sessionHandler,
            TransactionalAccountRefreshControllerFactory
                    transactionalAccountRefreshControllerFactory) {
        super(componentProvider);
        this.authenticationProcessFacade = authenticationProcessFacade;
        this.sessionHandler = sessionHandler;
        this.transactionalAccountRefreshController =
                transactionalAccountRefreshControllerFactory.create(
                        this.metricRefreshController, this.updateController);
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        return authenticationProcessFacade;
    }

    @Override
    public boolean isBackgroundRefreshPossible() {
        return true;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return sessionHandler;
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
}
