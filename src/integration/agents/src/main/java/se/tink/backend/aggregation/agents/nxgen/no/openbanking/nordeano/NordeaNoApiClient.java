package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordeano;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.*;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class NordeaNoApiClient extends NordeaBaseApiClient {

    public NordeaNoApiClient(
            TinkHttpClient client,
            SessionStorage sessionStorage,
            PersistentStorage persistentStorage) {
        super(client, sessionStorage, persistentStorage);
    }
}
