package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.LunarConstants.Storage;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationPersistedDataAccessor;

public class LunarAuthDataAccessor
        extends AgentJsonRepresentationAuthenticationPersistedDataAccessor<LunarAuthData> {

    LunarAuthDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData,
            ObjectMapper objectMapper) {
        super(agentAuthenticationPersistedData, objectMapper, LunarAuthData.class);
    }

    @Override
    protected String storeKey() {
        return Storage.PERSISTED_DATA_KEY;
    }

    public LunarAuthData get() {
        return getFromStorage().orElseGet(LunarAuthData::new);
    }

    public AgentAuthenticationPersistedData storeData(LunarAuthData value) {
        return super.store(value);
    }
}
