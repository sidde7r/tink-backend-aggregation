package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationPersistedDataAccessor;

public class BelfiusPersistedDataAccessor
        extends AgentJsonRepresentationAuthenticationPersistedDataAccessor<
                BelfiusAuthenticationData> {

    BelfiusPersistedDataAccessor(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData,
            ObjectMapper objectMapper) {
        super(agentAuthenticationPersistedData, objectMapper, BelfiusAuthenticationData.class);
    }

    @Override
    protected String storeKey() {
        return "BELFIUS_AUTHENTICATION_DATA";
    }

    public BelfiusAuthenticationData getBelfiusAuthenticationData() {
        return super.getFromStorage().orElse(new BelfiusAuthenticationData());
    }

    public AgentAuthenticationPersistedData storeBelfiusAuthenticationData(
            BelfiusAuthenticationData belfiusAuthenticationData) {
        return super.store(belfiusAuthenticationData);
    }
}
