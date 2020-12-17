package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro;

import com.google.inject.Inject;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAgent;
import se.tink.backend.aggregation.agents.module.annotation.AgentDependencyModules;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAbstractMultiStepsAuthenticationProcess;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

@AgentDependencyModules(modules = MetroModule.class)
public class MetroPersonalAgent extends AgentPlatformAgent {
    private final AgentAbstractMultiStepsAuthenticationProcess authenticationProcessFacade;

    private final SessionHandler sessionHandler;

    @Inject
    protected MetroPersonalAgent(
            AgentComponentProvider componentProvider,
            AgentAbstractMultiStepsAuthenticationProcess authenticationProcessFacade,
            SessionHandler sessionHandler) {
        super(componentProvider);
        this.authenticationProcessFacade = authenticationProcessFacade;
        this.sessionHandler = sessionHandler;
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
        return this.sessionHandler;
    }
}
