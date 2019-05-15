package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.authenticator;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class HandelsbankenBaseAuthenticator implements Authenticator {

    private final SessionStorage sessionStorage;

    public HandelsbankenBaseAuthenticator(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void authenticate(Credentials credentials) {
        sessionStorage.put(StorageKeys.ACCESS_TOKEN, credentials.getField("accessToken"));
    }
}
