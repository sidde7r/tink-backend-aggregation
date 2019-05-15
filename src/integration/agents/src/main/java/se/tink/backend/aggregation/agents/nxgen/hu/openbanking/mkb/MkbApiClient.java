package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb;

import se.tink.backend.aggregation.agents.nxgen.hu.openbanking.mkb.configuration.MkbConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.FintechblocksApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public final class MkbApiClient extends FintechblocksApiClient {
    private MkbConfiguration configuration;

    public MkbApiClient(TinkHttpClient client, PersistentStorage persistentStorage) {
        super(client, persistentStorage);
    }
}
