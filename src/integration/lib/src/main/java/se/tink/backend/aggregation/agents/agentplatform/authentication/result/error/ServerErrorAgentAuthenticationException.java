package se.tink.backend.aggregation.agents.agentplatform.authentication.result.error;

public class ServerErrorAgentAuthenticationException extends AgentAuthenticationException {

    public ServerErrorAgentAuthenticationException(
            AgentAuthenticationError agentAuthenticationError, String message) {
        super(agentAuthenticationError, message);
    }
}
