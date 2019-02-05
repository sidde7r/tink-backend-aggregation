package se.tink.backend.aggregation.agents.nxgen.be.banks.argenta;

import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ArgentaPersistentStorage {
    private PersistentStorage persistentStorage;

    public ArgentaPersistentStorage(PersistentStorage sessionStorage) {
        this.persistentStorage = sessionStorage;
    }

    public String getDeviceId() {
        return persistentStorage.get(ArgentaConstants.Storage.DEVICE_ID);
    }

    public void storeDeviceId(String deviceId) {
        persistentStorage.put(ArgentaConstants.Storage.DEVICE_ID, deviceId);
    }

    public void storeHomeOffice(String homeOfficeId) {
        persistentStorage.put(ArgentaConstants.Storage.HOME_OFFICE, homeOfficeId);
    }

    public void storeUak(String uak) {
        persistentStorage.put(ArgentaConstants.Storage.UAK, uak);
    }

    public String getHomeOffice() {
        return persistentStorage.get(ArgentaConstants.Storage.HOME_OFFICE);
    }

    public String getUak() {
        return persistentStorage.get(ArgentaConstants.Storage.UAK);
    }
}
