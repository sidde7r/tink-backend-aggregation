package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v21.NordeaV21ApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.rpc.Credentials;

public class NordeaFiApiClient extends NordeaV21ApiClient {

    public NordeaFiApiClient(TinkHttpClient client, Credentials credentials, String marketCode) {
        super(client, credentials, marketCode);
    }

}
