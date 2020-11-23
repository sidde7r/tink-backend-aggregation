package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;
import se.tink.backend.aggregation.agentsplatform.framework.error.AgentBankApiError;

public class AgentPlatformAuthenticationProcessException extends AuthenticationException {

    private AgentPlatformAuthenticationProcessError agentBaseError;

    public AgentPlatformAuthenticationProcessException(
            AgentPlatformAuthenticationProcessError agentPlatformAuthenticationError,
            String message) {
        super(agentPlatformAuthenticationError, message);
        agentBaseError = agentPlatformAuthenticationError;
    }

    @Override
    public AgentError getError() {
        return agentBaseError;
    }

    public AgentBankApiError getSourceAgentPlatformError() {
        return agentBaseError.getAgentBankApiError();
    }
}
