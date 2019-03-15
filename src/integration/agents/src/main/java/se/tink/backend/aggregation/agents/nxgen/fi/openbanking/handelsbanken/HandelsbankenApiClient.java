package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.handelsbanken;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbankenbase.HandelsbankenBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class HandelsbankenApiClient extends HandelsbankenBaseApiClient {

    public HandelsbankenApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);
    }
}
