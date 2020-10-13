package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.auth.persistence;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import lombok.AllArgsConstructor;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.agentplatform.authentication.storage.AgentPlatformStorageMigrator;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusConstants;
import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@AllArgsConstructor
public class BelfiusAgentPlatformStorageMigrator implements AgentPlatformStorageMigrator {

    private final Credentials credentials;
    private final ObjectMapper objectMapper;

    @Override
    public AgentAuthenticationPersistedData migrate(PersistentStorage persistentStorage) {
        BelfiusAuthenticationData belfiusAuthenticationData = new BelfiusAuthenticationData();

        credentials
                .getOptionalField(Field.Key.USERNAME)
                .ifPresent(belfiusAuthenticationData::setPanNumber);
        credentials
                .getOptionalField(Field.Key.PASSWORD)
                .ifPresent(belfiusAuthenticationData::setPassword);
        persistentStorage
                .getOptional(BelfiusConstants.Storage.DEVICE_TOKEN)
                .ifPresent(belfiusAuthenticationData::setDeviceToken);
        return new BelfiusPersistedData(
                        new AgentAuthenticationPersistedData(new HashMap<>()), objectMapper)
                .storeBelfiusAuthenticationData(belfiusAuthenticationData);
    }
}
