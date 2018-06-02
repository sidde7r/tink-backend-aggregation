package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SamlinkPersistentStorage {
    private PersistentStorage persistentStorage;

    public SamlinkPersistentStorage(PersistentStorage storage) {
        this.persistentStorage = storage;
    }

    public void putDeviceId(String deviceId) {
        Preconditions.checkNotNull(Strings.emptyToNull(deviceId), "Device id is null or empty");
        persistentStorage.put(SamlinkConstants.Storage.DEVICE_ID, deviceId);
    }

    public String getDeviceId() {
        return persistentStorage.get(SamlinkConstants.Storage.DEVICE_ID);
    }

    public void putDeviceToken(String deviceToken) {
        Preconditions.checkNotNull(Strings.emptyToNull(deviceToken), "Device token is null or empty");
        persistentStorage.put(SamlinkConstants.Storage.DEVICE_TOKEN, deviceToken);
    }

    public String getDeviceToken() {
        return persistentStorage.get(SamlinkConstants.Storage.DEVICE_TOKEN);
    }
}
