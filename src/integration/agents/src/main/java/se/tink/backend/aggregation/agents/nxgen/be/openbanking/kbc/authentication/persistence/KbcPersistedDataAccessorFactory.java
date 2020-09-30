package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;

public class KbcPersistedDataAccessorFactory {

    private final ObjectMapper objectMapper;

    public KbcPersistedDataAccessorFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public KbcPersistedData createKbcAuthenticationPersistedDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        return new KbcPersistedData(agentAuthenticationPersistedData, objectMapper);
    }
}
