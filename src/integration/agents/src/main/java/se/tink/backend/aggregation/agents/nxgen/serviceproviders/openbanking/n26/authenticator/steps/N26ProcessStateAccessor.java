package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationProcessStateAccessor;

public class N26ProcessStateAccessor
        extends AgentJsonRepresentationAuthenticationProcessStateAccessor<N26ProcessStateData> {

    private static final String KEY = "N26ProcessStateData";

    public N26ProcessStateAccessor(
            AgentAuthenticationProcessState agentAuthenticationProcessState,
            ObjectMapper objectMapper) {
        super(agentAuthenticationProcessState, objectMapper, N26ProcessStateData.class);
    }

    @Override
    protected String storeKey() {
        return KEY;
    }

    public N26ProcessStateData getN26ProcessStateData() {
        return super.getFromStorage().orElseGet(N26ProcessStateData::new);
    }

    public AgentAuthenticationProcessState storeN26ProcessStateData(
            N26ProcessStateData n26ProcessStateData) {
        return super.store(n26ProcessStateData);
    }
}
