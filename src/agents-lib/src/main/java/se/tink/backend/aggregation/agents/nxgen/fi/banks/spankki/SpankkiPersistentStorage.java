package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SpankkiPersistentStorage {

    private final PersistentStorage persistentStorage;

    public SpankkiPersistentStorage(PersistentStorage persistentStorage) {

        this.persistentStorage = persistentStorage;
    }

    public String getDeviceId() {
        return persistentStorage.get(SpankkiConstants.Storage.DEVICE_ID);
    }

    public void putDeviceId(String deviceId) {
        Preconditions.checkNotNull(deviceId);

        persistentStorage.put(SpankkiConstants.Storage.DEVICE_ID, deviceId);
    }

    public String getDeviceToken() {
        return persistentStorage.get(SpankkiConstants.Storage.DEVICE_TOKEN);
    }

    public void putDeviceToken(String deviceToken) {
        Preconditions.checkNotNull(deviceToken);

        persistentStorage.put(SpankkiConstants.Storage.DEVICE_TOKEN, deviceToken);
    }
}
