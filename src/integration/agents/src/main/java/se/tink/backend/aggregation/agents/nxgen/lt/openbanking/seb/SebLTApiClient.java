package se.tink.backend.aggregation.agents.nxgen.lt.openbanking.seb;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbalticsbase.SebBalticsBaseApiClient;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;

public class SebLTApiClient extends SebBalticsBaseApiClient {

    public SebLTApiClient(
            TinkHttpClient client,
            PersistentStorage persistentStorage,
            CredentialsRequest credentialsRequest) {
        super(client, persistentStorage, credentialsRequest);
    }

    @Override
    public String getProviderMarketCode() {
        return SebLTConstants.MARKET_CODE;
    }

    @Override
    public String getBic() {
        return SebLTConstants.BIC;
    }
}
