package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;

public class AgentAuthenticationException extends AuthenticationException {

    private AgentAuthenticationError agentBaseError;

    public AgentAuthenticationException(
            AgentAuthenticationError agentAuthenticationError, String message) {
        super(agentAuthenticationError, message);
        agentBaseError = agentAuthenticationError;
    }

    @Override
    public AgentError getError() {
        return agentBaseError;
    }
}
