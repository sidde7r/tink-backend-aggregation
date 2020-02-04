package se.tink.backend.aggregation.agents.common;

import se.tink.backend.aggregation.agents.exceptions.agent.AgentBaseError;

public class RequestException extends Exception {

    private AgentBaseError agentError;
    private String message;

    public RequestException(AgentBaseError agentError, String message) {
        this.agentError = agentError;
        this.message = message;
    }

    public RequestException(String message) {
        this.message = message;
    }

    public AgentBaseError getAgentError() {
        return agentError;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
