package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;

public class BelfiusPersistedDataAccessorFactory {

    private final ObjectMapper objectMapper;

    public BelfiusPersistedDataAccessorFactory(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public BelfiusPersistedData createBelfiusPersistedDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        return new BelfiusPersistedData(agentAuthenticationPersistedData, objectMapper);
    }
}
