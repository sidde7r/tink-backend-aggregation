package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.storage;

import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class LclPersistentStorage {

    private final PersistentStorage persistentStorage;

    public LclPersistentStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public void saveDeviceId(String deviceId) {
        persistentStorage.put(LclConstants.Storage.DEVICE_ID, deviceId);
    }

    public String getDeviceId() {
        return persistentStorage.get(LclConstants.Storage.DEVICE_ID);
    }

    public void saveAgentKey(String agentKey) {
        persistentStorage.put(LclConstants.Storage.AGENT_KEY, agentKey);
    }

    public String getAgentKey() {
        return persistentStorage.get(LclConstants.Storage.AGENT_KEY);
    }
}
