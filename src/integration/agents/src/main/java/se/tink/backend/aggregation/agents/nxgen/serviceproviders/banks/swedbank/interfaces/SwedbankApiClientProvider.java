package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.interfaces;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public interface SwedbankApiClientProvider {

    public <T extends SwedbankDefaultApiClient> T getApiAgent(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            Credentials credentials,
            SessionStorage sessionStorage);
}
