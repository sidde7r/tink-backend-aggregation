package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.sberbank.configuration.SberBankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class SberBankApiClient extends FintechblocksApiClient {
    private SberBankConfiguration configuration;

    public SberBankApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);
    }
}
