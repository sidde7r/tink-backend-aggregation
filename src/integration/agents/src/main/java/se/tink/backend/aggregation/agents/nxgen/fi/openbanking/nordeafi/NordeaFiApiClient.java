package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordeafi;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class NordeaFiApiClient extends NordeaBaseApiClient {

    public NordeaFiApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        super(client, sessionStorage, persistentStorage);
    }
}
