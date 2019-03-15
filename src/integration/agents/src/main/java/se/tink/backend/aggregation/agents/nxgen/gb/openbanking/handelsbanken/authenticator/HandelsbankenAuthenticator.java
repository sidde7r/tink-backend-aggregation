package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken.authenticator;

import se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken.HandelsbankenApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.authenticator.HandelsbankenBaseAuthenticator;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;


public class HandelsbankenAuthenticator extends HandelsbankenBaseAuthenticator {

    public HandelsbankenAuthenticator(HandelsbankenApiClient apiClient,
        PersistentStorage persistentStorage) {
        super(apiClient, persistentStorage);
    }

}
