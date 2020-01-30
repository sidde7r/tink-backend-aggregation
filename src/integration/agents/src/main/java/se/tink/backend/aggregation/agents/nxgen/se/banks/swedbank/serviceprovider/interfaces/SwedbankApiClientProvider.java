package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.interfaces;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public interface SwedbankApiClientProvider {

    <T extends SwedbankDefaultApiClient> T getApiAgent(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            Credentials credentials,
            SwedbankStorage swedbankStorage);
}
