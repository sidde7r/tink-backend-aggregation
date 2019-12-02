package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.interfaces;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankStorage;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;

public interface SwedbankApiClientProvider {

    public <T extends SwedbankDefaultApiClient> T getApiAgent(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            Credentials credentials,
            SwedbankStorage swedbankStorage);
}
