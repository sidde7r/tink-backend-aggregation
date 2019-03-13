package se.tink.backend.integration.fetchservice.fetchcontroller;

class AgentState {
    private String state;

    private AgentState(String state) {
        this.state = state;
    }

    static AgentState of(se.tink.backend.integration.api.models.AgentState state) {
        return new AgentState(state.getState());
    }
}
