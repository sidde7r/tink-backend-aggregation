package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyske;

import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class JyskePersistentStorage {
    private final PersistentStorage persistentStorage;

    public JyskePersistentStorage(PersistentStorage storage) {
        this.persistentStorage = storage;
    }

    public boolean readyForSingleFactor() {
        return containsKey(JyskeConstants.Storage.INSTALL_ID);
    }

    private boolean containsKey(String key) {
        return persistentStorage.get(key) != null;
    }

    public String getInstallId() {
        return persistentStorage.get(JyskeConstants.Storage.INSTALL_ID);
    }

    public void persist(String installId) {
        persistentStorage.put(JyskeConstants.Storage.INSTALL_ID, installId);
    }
}
