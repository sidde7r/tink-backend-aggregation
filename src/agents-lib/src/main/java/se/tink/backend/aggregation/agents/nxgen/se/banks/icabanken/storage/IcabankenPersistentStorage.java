package se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.storage;

import se.tink.backend.aggregation.agents.nxgen.se.banks.icabanken.IcaBankenConstants;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class IcabankenPersistentStorage {
    private final PersistentStorage persistentStorage;

    public IcabankenPersistentStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public void saveDeviceApplicationId(String deviceApplicationId) {
        persistentStorage.put(IcaBankenConstants.IdTags.DEVICE_APPLICATION_ID, deviceApplicationId);
    }

    public String getDeviceApplicationId() {
        return persistentStorage.get(IcaBankenConstants.IdTags.DEVICE_APPLICATION_ID);
    }

    public void saveUserInstallationId(String userInstallationId) {
        persistentStorage.put(IcaBankenConstants.IdTags.USER_INSTALLATION_ID, userInstallationId);
    }

    public String getUserInstallationId() {
        return persistentStorage.get(IcaBankenConstants.IdTags.USER_INSTALLATION_ID);
    }
}
