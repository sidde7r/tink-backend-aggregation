package se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.authenticator;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.authenticator.HandelsbankenBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;


public class HandelsbankenAuthenticator extends HandelsbankenBaseAuthenticator {

    public HandelsbankenAuthenticator(HandelsbankenApiClient apiClient,
        SessionStorage sessionStorage) {
        super(apiClient, sessionStorage);
    }

}
