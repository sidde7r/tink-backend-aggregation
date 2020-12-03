package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationProcessStateAccessor;

public class FortisProcessStateAccessor
        extends AgentJsonRepresentationAuthenticationProcessStateAccessor<FortisProcessState> {

    public FortisProcessStateAccessor(
            AgentAuthenticationProcessState agentAuthenticationProcessState,
            ObjectMapper objectMapper) {
        super(agentAuthenticationProcessState, objectMapper, FortisProcessState.class);
    }

    @Override
    protected String storeKey() {
        return "FortisProcessState";
    }

    public FortisProcessState get() {
        return super.getFromStorage().orElseGet(FortisProcessState::new);
    }

    @Override
    public AgentAuthenticationProcessState store(FortisProcessState value) {
        return super.store(value);
    }
}
