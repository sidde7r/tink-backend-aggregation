package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.granitbank;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.granitbank.configuration.GranitBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class GranitBankApiClient extends FintechblocksApiClient {
    private GranitBankConfiguration configuration;

    public GranitBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);
    }
}
