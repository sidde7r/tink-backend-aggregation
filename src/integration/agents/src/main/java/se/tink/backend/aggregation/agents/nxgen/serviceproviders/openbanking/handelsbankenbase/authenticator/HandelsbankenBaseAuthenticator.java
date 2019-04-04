package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenBaseAuthenticator implements Authenticator {

    protected final HandelsbankenBaseApiClient apiClient;
    protected final SessionStorage sessionStorage;

    public HandelsbankenBaseAuthenticator(
            HandelsbankenBaseApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials) {
        sessionStorage.put(StorageKeys.ACCESS_TOKEN, "MV9QUk9GSUxFLUZJX1BSSVZBVEUz");
    }
}
