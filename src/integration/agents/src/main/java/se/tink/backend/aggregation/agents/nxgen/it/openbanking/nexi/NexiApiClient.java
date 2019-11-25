package se.tink.backend.aggregation.agents.nxgen.it.openbanking.nexi;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class NexiApiClient extends CbiGlobeApiClient {

    public NexiApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            boolean requestManual,
            TemporaryStorage temporaryStorage) {
        super(
                client,
                persistentStorage,
                requestManual,
                temporaryStorage,
                InstrumentType.CARDS_ACCOUNTS);
    }
}
