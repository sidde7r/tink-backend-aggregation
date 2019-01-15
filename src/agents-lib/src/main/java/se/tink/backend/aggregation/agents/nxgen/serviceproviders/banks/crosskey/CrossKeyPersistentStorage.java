package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.AddDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.crosskey.authenticator.rpc.LoginWithTokenResponse;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class CrossKeyPersistentStorage {
    private final PersistentStorage persistentStorage;

    public CrossKeyPersistentStorage(PersistentStorage persistentStorage) {
        this.persistentStorage = persistentStorage;
    }

    public boolean readyForSingleFactor() {
        return containsKey(CrossKeyConstants.Storage.DEVICE_ID) &&
                containsKey(CrossKeyConstants.Storage.DEVICE_TOKEN);
    }

    private boolean containsKey(String key) {
        return persistentStorage.get(key) != null;
    }

    public String getDeviceToken() {
        return persistentStorage.get(CrossKeyConstants.Storage.DEVICE_TOKEN);
    }

    public String getDeviceId() {
        return persistentStorage.get(CrossKeyConstants.Storage.DEVICE_ID);
    }

    public void clearDeviceCredentials() {
        persistentStorage.remove(CrossKeyConstants.Storage.DEVICE_TOKEN);
        persistentStorage.remove(CrossKeyConstants.Storage.DEVICE_ID);
    }

    public void persist(AddDeviceResponse addDeviceResponse) {
        storeToken(addDeviceResponse.getToken());
        persistentStorage.put(CrossKeyConstants.Storage.DEVICE_ID, addDeviceResponse.getDeviceId());
    }

    public void persist(LoginWithTokenResponse response) {
        storeToken(response.getDeviceToken());
    }

    private void storeToken(String token) {
        persistentStorage.put(CrossKeyConstants.Storage.DEVICE_TOKEN, token);
    }
}
