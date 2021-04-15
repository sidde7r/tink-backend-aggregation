package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class NordeaFiApiClient extends NordeaBaseApiClient {

    public NordeaFiApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, QsealcSigner qsealcSigner) {
        super(client, persistentStorage, qsealcSigner, false);
    }
}
