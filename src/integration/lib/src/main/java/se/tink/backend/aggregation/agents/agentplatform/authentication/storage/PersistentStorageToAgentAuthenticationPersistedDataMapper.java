package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PersistentStorageToAgentAuthenticationPersistedDataMapper {

    public AgentAuthenticationPersistedData mapTo(final PersistentStorage persistentStorage) {
        return new AgentAuthenticationPersistedData(persistentStorage);
    }

    public PersistentStorage mapFrom(
            final AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        PersistentStorage persistentStorage = new PersistentStorage();
        agentAuthenticationPersistedData
                .valuesCopy()
                .entrySet()
                .forEach(entry -> persistentStorage.put(entry.getKey(), entry.getValue()));
        return persistentStorage;
    }
}
