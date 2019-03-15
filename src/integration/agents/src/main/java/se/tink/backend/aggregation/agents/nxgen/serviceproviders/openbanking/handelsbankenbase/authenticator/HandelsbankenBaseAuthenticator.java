package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;


public class HandelsbankenBaseAuthenticator implements Authenticator {

    protected final HandelsbankenBaseApiClient apiClient;
    protected final PersistentStorage persistentStorage;

    public HandelsbankenBaseAuthenticator(HandelsbankenBaseApiClient apiClient,
        PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void authenticate(Credentials credentials) {
        persistentStorage.put(StorageKeys.ACCESS_TOKEN, "MVBST0ZJTEVfTkxfUFJJVkFURV8z");
    }
}
