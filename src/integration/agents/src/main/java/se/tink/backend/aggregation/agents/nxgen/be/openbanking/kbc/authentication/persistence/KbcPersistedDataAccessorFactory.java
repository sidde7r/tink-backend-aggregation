package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;

@AllArgsConstructor
public class KbcPersistedDataAccessorFactory {

    private final ObjectMapper objectMapper;

    public KbcPersistedData createKbcAuthenticationPersistedDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        return new KbcPersistedData(agentAuthenticationPersistedData, objectMapper);
    }
}
