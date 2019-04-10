package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public final class NordeaDkApiClient extends NordeaBaseApiClient {

    public NordeaDkApiClient(TinkHttpClient client, SessionStorage sessionStorage) {
        super(client, sessionStorage);
    }
}
