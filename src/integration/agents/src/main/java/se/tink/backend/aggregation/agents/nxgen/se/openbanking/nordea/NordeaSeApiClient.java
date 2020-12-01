package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class NordeaSeApiClient extends NordeaBaseApiClient {

    public NordeaSeApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            QsealcSigner qsealcSigner,
            String providerName) {
        super(client, persistentStorage, qsealcSigner, providerName, false);
    }
}
