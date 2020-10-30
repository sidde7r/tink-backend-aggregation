package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.BelfiusProcessStateAccessor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;

@AllArgsConstructor
public class BelfiusDataAccessorFactory {

    private final ObjectMapper objectMapper;

    public BelfiusPersistedDataAccessor createBelfiusPersistedDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        return new BelfiusPersistedDataAccessor(agentAuthenticationPersistedData, objectMapper);
    }

    public BelfiusProcessStateAccessor createBelfiusProcessStateAccessor(
            AgentAuthenticationProcessState authenticationProcessState) {
        return new BelfiusProcessStateAccessor(authenticationProcessState, objectMapper);
    }
}
