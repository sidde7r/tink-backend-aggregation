package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationProcessStateAccessor;

public class LunarProcessStateAccessor
        extends AgentJsonRepresentationAuthenticationProcessStateAccessor<LunarProcessState> {

    LunarProcessStateAccessor(
            AgentAuthenticationProcessState agentAuthenticationProcessState,
            ObjectMapper objectMapper) {
        super(agentAuthenticationProcessState, objectMapper, LunarProcessState.class);
    }

    @Override
    protected String storeKey() {
        return "LunarProcessState";
    }

    public LunarProcessState get() {
        return super.getFromStorage().orElseGet(LunarProcessState::new);
    }

    public AgentAuthenticationProcessState storeState(LunarProcessState value) {
        return super.store(value);
    }
}
