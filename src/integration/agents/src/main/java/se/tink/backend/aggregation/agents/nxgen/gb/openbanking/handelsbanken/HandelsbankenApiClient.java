package se.tink.backend.aggregation.agents.nxgen.gb.openbanking.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class HandelsbankenApiClient extends HandelsbankenBaseApiClient {

    public HandelsbankenApiClient(TinkHttpClient client, PersistentStorage persistentStorage,
        SessionStorage sessionStorage) {
        super(client, persistentStorage, sessionStorage);
    }
}
