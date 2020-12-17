package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationProcessStateAccessor;

public class MetroProcessStateAccessor
        extends AgentJsonRepresentationAuthenticationProcessStateAccessor<MetroProcessState> {

    public MetroProcessStateAccessor(
            AgentAuthenticationProcessState agentAuthenticationProcessState,
            ObjectMapper objectMapper) {
        super(agentAuthenticationProcessState, objectMapper, MetroProcessState.class);
    }

    public MetroProcessState getProcessState() {
        return super.getFromStorage().orElseGet(MetroProcessState::new);
    }

    public AgentAuthenticationProcessState storeProcessState(MetroProcessState processState) {
        return store(processState);
    }

    @Override
    protected String storeKey() {
        return "MetroProcessState";
    }
}
