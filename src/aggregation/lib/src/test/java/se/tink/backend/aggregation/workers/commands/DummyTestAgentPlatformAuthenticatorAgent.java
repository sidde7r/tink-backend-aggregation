package se.tink.backend.aggregation.workers.commands;

import jdk.nashorn.internal.ir.annotations.Ignore;
import se.tink.backend.aggregation.agents.agentplatform.authentication.AgentPlatformAuthenticator;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;
import se.tink.backend.aggregation.nxgen.agents.SubsequentProgressiveGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.controllers.authentication.progressive.StatelessProgressiveAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Ignore
public class DummyTestAgentPlatformAuthenticatorAgent extends SubsequentProgressiveGenerationAgent
        implements AgentPlatformAuthenticator {

    private AgentAuthenticationProcess agentAuthenticationProcess;

    public DummyTestAgentPlatformAuthenticatorAgent(
            AgentComponentProvider componentProvider,
            AgentAuthenticationProcess agentAuthenticationProcess) {
        super(componentProvider);
        this.agentAuthenticationProcess = agentAuthenticationProcess;
    }

    @Override
    public AgentAuthenticationProcess getAuthenticationProcess() {
        return agentAuthenticationProcess;
    }

    @Override
    public boolean isBackgroundRefreshPossible() {
        return false;
    }

    @Override
    protected SessionHandler constructSessionHandler() {
        return null;
    }

    @Override
    public StatelessProgressiveAuthenticator getAuthenticator() {
        throw new RuntimeException(
                "This method shouldn'b be executed for the AgentPlatformAuthenticator");
    }

    @Override
    protected PersistentStorage getPersistentStorage() {
        return super.getPersistentStorage();
    }
}
