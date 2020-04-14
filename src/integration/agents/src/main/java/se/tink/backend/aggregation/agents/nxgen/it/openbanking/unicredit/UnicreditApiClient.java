package se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.UnicreditBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class UnicreditApiClient extends UnicreditBaseApiClient {

    UnicreditApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            Credentials credentials,
            boolean manualRequest) {
        super(client, persistentStorage, credentials, manualRequest);
    }
}
