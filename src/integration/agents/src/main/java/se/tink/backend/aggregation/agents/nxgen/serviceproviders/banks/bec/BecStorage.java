package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys.DEVICE_ID_STORAGE_KEY;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecConstants.StorageKeys.SCA_TOKEN_STORAGE_KEY;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class BecStorage {

    private final PersistentStorage persistentStorage;

    public void saveScaToken(String scaToken) {
        persistentStorage.put(SCA_TOKEN_STORAGE_KEY, scaToken);
    }

    public String getScaToken() {
        return persistentStorage.get(SCA_TOKEN_STORAGE_KEY);
    }

    private void removeScaToken() {
        persistentStorage.remove(SCA_TOKEN_STORAGE_KEY);
    }

    public void saveDeviceId(String deviceId) {
        persistentStorage.put(DEVICE_ID_STORAGE_KEY, deviceId);
    }

    public String getDeviceId() {
        return persistentStorage.get(DEVICE_ID_STORAGE_KEY);
    }

    private void removeDeviceId() {
        persistentStorage.remove(DEVICE_ID_STORAGE_KEY);
    }

    public void clearSessionData() {
        removeScaToken();
        removeDeviceId();
    }
}
