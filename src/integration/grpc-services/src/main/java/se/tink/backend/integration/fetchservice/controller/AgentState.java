package se.tink.backend.integration.fetchservice.controller;

class AgentState {
    private String state;

    private AgentState(String state) {
        this.state = state;
    }

    static AgentState of(String state) {
        return new AgentState(state);
    }
}
