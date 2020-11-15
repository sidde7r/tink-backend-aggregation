package se.tink.backend.aggregation.agents.nxgen.it.openbanking.nexi;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.CbiGlobeProviderConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.configuration.InstrumentType;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class NexiApiClient extends CbiGlobeApiClient {

    public NexiApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            boolean requestManual,
            TemporaryStorage temporaryStorage,
            CbiGlobeProviderConfiguration providerConfiguration,
            String psuIpAddress) {
        super(
                client,
                persistentStorage,
                sessionStorage,
                temporaryStorage,
                InstrumentType.CARDS_ACCOUNTS,
                providerConfiguration,
                requestManual ? psuIpAddress : null);
    }
}
