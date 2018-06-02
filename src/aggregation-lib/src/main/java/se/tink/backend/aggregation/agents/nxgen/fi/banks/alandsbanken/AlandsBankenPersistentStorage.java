package se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.alandsbanken.authenticator.rpc.LoginWithTokenResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class AlandsBankenPersistentStorage {
    private final PersistentStorage persistentStorage;

    public AlandsBankenPersistentStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public boolean readyForSingleFactor() {
        return containsKey(AlandsBankenConstants.Storage.DEVICE_ID) &&
                containsKey(AlandsBankenConstants.Storage.DEVICE_TOKEN);
    }

    private boolean containsKey(String key) {
        return persistentStorage.get(key) != null;
    }

    public String getDeviceToken() {
        return persistentStorage.get(AlandsBankenConstants.Storage.DEVICE_TOKEN);
    }

    public String getDeviceId() {
        return persistentStorage.get(AlandsBankenConstants.Storage.DEVICE_ID);
    }

    public void clearDeviceCredentials() {
        persistentStorage.remove(AlandsBankenConstants.Storage.DEVICE_TOKEN);
        persistentStorage.remove(AlandsBankenConstants.Storage.DEVICE_ID);
    }

    public void persist(AddDeviceResponse addDeviceResponse) {
        storeToken(addDeviceResponse.getToken());
        persistentStorage.put(AlandsBankenConstants.Storage.DEVICE_ID, addDeviceResponse.getDeviceId());
    }

    public void persist(LoginWithTokenResponse response) {
        storeToken(response.getDeviceToken());
    }

    private void storeToken(String token) {
        persistentStorage.put(AlandsBankenConstants.Storage.DEVICE_TOKEN, token);
    }
}
