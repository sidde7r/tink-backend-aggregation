package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.n26.authenticator.steps.fetch_consent;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationPersistedDataAccessor;

public class N26ConsentAccessor
        extends AgentJsonRepresentationAuthenticationPersistedDataAccessor<
                N26ConsentPersistentData> {

    public N26ConsentAccessor(
            AgentAuthenticationPersistedData agentAuthenticationProcessState,
            ObjectMapper objectMapper) {
        super(agentAuthenticationProcessState, objectMapper, N26ConsentPersistentData.class);
    }

    @Override
    protected String storeKey() {
        return "N26ConsentPersistentData";
    }

    public N26ConsentPersistentData getN26ConsentPersistentData() {
        return super.getFromStorage().orElseGet(N26ConsentPersistentData::new);
    }

    public AgentAuthenticationPersistedData storeN26ConsentPersistentData(
            N26ConsentPersistentData n26ConsentPersistentData) {
        return super.store(n26ConsentPersistentData);
    }
}
