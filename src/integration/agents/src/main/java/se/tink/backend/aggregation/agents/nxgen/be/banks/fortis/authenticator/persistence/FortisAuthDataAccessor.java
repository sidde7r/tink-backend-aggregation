package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationPersistedDataAccessor;

public class FortisAuthDataAccessor
        extends AgentJsonRepresentationAuthenticationPersistedDataAccessor<FortisAuthData> {

    public FortisAuthDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData,
            ObjectMapper objectMapper) {
        super(agentAuthenticationPersistedData, objectMapper, FortisAuthData.class);
    }

    @Override
    protected String storeKey() {
        return "FortisAuthData";
    }

    public FortisAuthData get() {
        return getFromStorage().orElseGet(FortisAuthData::new);
    }

    @Override
    public AgentAuthenticationPersistedData store(FortisAuthData value) {
        return super.store(value);
    }
}
