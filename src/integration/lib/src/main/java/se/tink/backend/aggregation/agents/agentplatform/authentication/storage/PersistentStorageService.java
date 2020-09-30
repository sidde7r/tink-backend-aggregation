package se.tink.backend.aggregation.agents.agentplatform.authentication.storage;

import se.tink.backend.aggregation.agentsplatform.agentsframework.authentication.process.AgentAuthenticationPersistedData;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class PersistentStorageService {

    private final PersistentStorageToAgentAuthenticationPersistedDataMapper persistedDataMapper;
    protected final PersistentStorage agentPersistentStorage;

    public PersistentStorageService(PersistentStorage agentPersistentStorage) {
        this.persistedDataMapper = new PersistentStorageToAgentAuthenticationPersistedDataMapper();
        this.agentPersistentStorage = agentPersistentStorage;
    }

    public void writeToAgentPersistentStorage(
            final AgentAuthenticationPersistedData agentAuthenticationPersistedData) {
        PersistentStorage persistentStorage =
                persistedDataMapper.mapFrom(agentAuthenticationPersistedData);
        agentPersistentStorage.clear();
        persistentStorage
                .entrySet()
                .forEach(entry -> agentPersistentStorage.put(entry.getKey(), entry.getValue()));
    }

    public AgentAuthenticationPersistedData readFromAgentPersistentStorage() {
        return persistedDataMapper.mapTo(agentPersistentStorage);
    }
}
