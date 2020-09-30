package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentJsonRepresentationAuthenticationPersistedDataAccessor;

public class KbcPersistedData
        extends AgentJsonRepresentationAuthenticationPersistedDataAccessor<KbcAuthenticationData> {

    KbcPersistedData(
            AgentAuthenticationPersistedData agentAuthenticationPersistedData,
            ObjectMapper objectMapper) {
        super(agentAuthenticationPersistedData, objectMapper, KbcAuthenticationData.class);
    }

    @Override
    protected String storeKey() {
        return "KBC_AUTHENTICATION_DATA";
    }

    public KbcAuthenticationData getKbcAuthenticationData() {
        return super.getFromStorage().orElseGet(KbcAuthenticationData::new);
    }

    public AgentAuthenticationPersistedData storeKbcAuthenticationData(
            KbcAuthenticationData kbcAuthenticationData) {
        return super.store(kbcAuthenticationData);
    }
}
