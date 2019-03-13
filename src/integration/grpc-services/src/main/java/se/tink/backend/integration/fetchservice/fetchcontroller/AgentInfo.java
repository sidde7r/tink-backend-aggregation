package se.tink.backend.integration.fetchservice.fetchcontroller;

class AgentInfo {
    private final String agentClassName;
    private final AgentState agentState;

    private AgentInfo(String agentClassName, AgentState agentState) {
        this.agentClassName = agentClassName;
        this.agentState = agentState;
    }

    static AgentInfo of(se.tink.backend.integration.api.models.AgentInfo agentInfo) {
        return new AgentInfo(
                agentInfo.getAgentClassName(),
                AgentState.of(agentInfo.getState()));
    }
}
