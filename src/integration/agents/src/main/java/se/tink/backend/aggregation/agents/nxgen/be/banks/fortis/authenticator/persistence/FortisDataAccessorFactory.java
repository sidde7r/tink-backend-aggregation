package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;

@RequiredArgsConstructor
public class FortisDataAccessorFactory {
    private final ObjectMapper objectMapper;

    public FortisAuthDataAccessor createAuthDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        return new FortisAuthDataAccessor(agentAuthenticationPersistedData, objectMapper);
    }

    public FortisProcessStateAccessor createProcessStateAccessor(
            AgentAuthenticationProcessState authenticationProcessState) {
        return new FortisProcessStateAccessor(authenticationProcessState, objectMapper);
    }
}
