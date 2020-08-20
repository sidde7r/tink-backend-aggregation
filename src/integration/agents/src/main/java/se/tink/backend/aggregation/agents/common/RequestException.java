package se.tink.backend.aggregation.agents.common;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentError;

public class RequestException extends Exception {

    private AgentError agentError;
    private String message;

    public RequestException(AgentError agentError, String message) {
        this.agentError = agentError;
        this.message = message;
    }

    public RequestException(String message) {
        this.message = message;
    }

    public AgentError getAgentError() {
        return agentError;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
