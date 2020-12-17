package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;

@AllArgsConstructor
public class MetroDataAccessorFactory {

    private final ObjectMapper objectMapper;

    public MetroPersistedDataAccessor createPersistedDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        return new MetroPersistedDataAccessor(agentAuthenticationPersistedData, objectMapper);
    }

    public MetroProcessStateAccessor createProcessStateAccessor(
            AgentAuthenticationProcessState authenticationProcessState) {
        return new MetroProcessStateAccessor(authenticationProcessState, objectMapper);
    }
}
