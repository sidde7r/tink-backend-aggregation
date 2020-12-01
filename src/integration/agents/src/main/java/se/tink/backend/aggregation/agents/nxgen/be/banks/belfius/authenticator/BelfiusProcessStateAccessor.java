package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.authenticator;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationProcessStateAccessor;

public class BelfiusProcessStateAccessor
        extends AgentJsonRepresentationAuthenticationProcessStateAccessor<BelfiusProcessState> {

    public BelfiusProcessStateAccessor(
            AgentAuthenticationProcessState agentAuthenticationProcessState,
            ObjectMapper objectMapper) {
        super(agentAuthenticationProcessState, objectMapper, BelfiusProcessState.class);
    }

    public BelfiusProcessState getBelfiusProcessState() {
        return super.getFromStorage().orElseGet(BelfiusProcessState::new);
    }

    public AgentAuthenticationProcessState storeBelfiusProcessState(
            BelfiusProcessState belfiusProcessState) {
        return store(belfiusProcessState);
    }

    @Override
    protected String storeKey() {
        return "BelfiusProcessState";
    }
}
