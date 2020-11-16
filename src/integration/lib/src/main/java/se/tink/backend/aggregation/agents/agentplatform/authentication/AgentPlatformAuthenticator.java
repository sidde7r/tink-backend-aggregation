package se.tink.backend.aggregation.agents.agentplatform.authentication;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcess;

public interface AgentPlatformAuthenticator {

    AgentAuthenticationProcess getAuthenticationProcess();

    boolean isBackgroundRefreshPossible();
}
