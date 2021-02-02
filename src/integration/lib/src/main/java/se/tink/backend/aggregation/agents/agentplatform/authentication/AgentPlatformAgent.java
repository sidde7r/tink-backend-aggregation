package se.tink.backend.aggregation.agents.agentplatform.authentication;

import se.tink.backend.aggregation.nxgen.agents.SubsequentGenerationAgent;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;

public abstract class AgentPlatformAgent extends SubsequentGenerationAgent
        implements AgentPlatformAuthenticator {

    protected AgentPlatformAgent(AgentComponentProvider componentProvider) {
        super(componentProvider);
    }

    @Override
    public final Object getAuthenticator() {
        throw new IllegalStateException(
                "Method should not be call for the Agent Platform authentication flow");
    }

    @Override
    public final boolean login() throws Exception {
        throw new IllegalStateException(
                "Method should not be call for the Agent Platform authentication flow");
    }
}
