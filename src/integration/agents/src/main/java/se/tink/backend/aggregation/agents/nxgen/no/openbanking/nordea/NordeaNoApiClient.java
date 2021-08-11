package se.tink.backend.aggregation.agents.nxgen.no.openbanking.nordea;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class NordeaNoApiClient extends NordeaBaseApiClient {

    public NordeaNoApiClient(
            AgentComponentProvider componentProvider,
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            QsealcSigner qsealcSigner) {
        super(componentProvider, client, persistentStorage, qsealcSigner, false);
    }
}
