package se.tink.backend.aggregation.agents.nxgen.lv.openbanking.seb;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebLVApiClient extends SebBalticsBaseApiClient {

    public SebLVApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            CredentialsRequest credentialsRequest) {
        super(client, persistentStorage, credentialsRequest);
    }

    @Override
    public String getProviderMarketCode() {
        return SebLVConstants.MARKET_CODE;
    }

    @Override
    public String getBic() {
        return SebLVConstants.BIC;
    }
}
