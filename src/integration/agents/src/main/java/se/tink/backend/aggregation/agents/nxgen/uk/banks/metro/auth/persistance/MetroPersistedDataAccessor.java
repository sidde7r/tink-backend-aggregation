package se.tink.backend.aggregation.agents.nxgen.uk.banks.metro.auth.persistance;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationPersistedDataAccessor;

public class MetroPersistedDataAccessor
        extends AgentJsonRepresentationAuthenticationPersistedDataAccessor<
                MetroAuthenticationData> {

    public MetroPersistedDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData,
            ObjectMapper objectMapper) {
        super(agentAuthenticationPersistedData, objectMapper, MetroAuthenticationData.class);
    }

    @Override
    protected String storeKey() {
        return "METRO_AUTHENTICATION_DATA";
    }

    public MetroAuthenticationData getAuthenticationData() {
        return super.getFromStorage().orElse(new MetroAuthenticationData());
    }

    public AgentAuthenticationPersistedData storeAuthenticationData(
            MetroAuthenticationData authenticationData) {
        return super.store(authenticationData);
    }
}
