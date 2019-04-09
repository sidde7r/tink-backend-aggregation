package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.interfaces.SwedbankApiClientProvider;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class SwedbankDefaultApiClientProvider implements SwedbankApiClientProvider {
    @Override
    public SwedbankDefaultApiClient getApiAgent(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            Credentials credentials,
            SessionStorage sessionStorage) {
        return new SwedbankSEApiClient(
                client, configuration, credentials.getField(Field.Key.USERNAME), sessionStorage);
    }
}
