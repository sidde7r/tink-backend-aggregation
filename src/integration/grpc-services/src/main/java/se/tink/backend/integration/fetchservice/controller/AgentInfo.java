package se.tink.backend.integration.fetchservice.controller;

public class AgentInfo {
    private final String agentClassName;
    private final AgentState agentState;

    private AgentInfo(String agentClassName, AgentState agentState) {
        this.agentClassName = agentClassName;
        this.agentState = agentState;
    }

    public static AgentInfo of(String className, String state) {
        return new AgentInfo(className, AgentState.of(state));
    }

    public String getAgentClassName() {
        return agentClassName;
    }

    public AgentState getAgentState() {
        return agentState;
    }
}
