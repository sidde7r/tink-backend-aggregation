package se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.nordea.filter.NordeaSeFilter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.nordeabase.NordeaBaseApiClient;
import se.tink.backend.aggregation.eidassigner.QsealcSigner;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.filter.filters.TimeoutFilter;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class NordeaSeApiClient extends NordeaBaseApiClient {

    public NordeaSeApiClient(
            TinkHttpClient client, PersistentStorage persistentStorage, QsealcSigner qsealcSigner) {
        super(client, persistentStorage, qsealcSigner, false);

        this.client.addFilter(new NordeaSeFilter());
        this.client.addFilter(new TimeoutFilter());
    }
}
