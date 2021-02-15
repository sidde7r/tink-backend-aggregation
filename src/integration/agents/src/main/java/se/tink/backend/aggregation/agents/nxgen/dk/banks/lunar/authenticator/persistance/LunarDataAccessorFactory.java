package se.tink.backend.aggregation.agents.nxgen.dk.banks.lunar.authenticator.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationProcessState;

@RequiredArgsConstructor
public class LunarDataAccessorFactory {

    private final ObjectMapper objectMapper;

    public LunarAuthDataAccessor createAuthDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        return new LunarAuthDataAccessor(agentAuthenticationPersistedData, objectMapper);
    }

    public LunarProcessStateAccessor createProcessStateAccessor(
            AgentAuthenticationProcessState authenticationProcessState) {
        return new LunarProcessStateAccessor(authenticationProcessState, objectMapper);
    }
}
