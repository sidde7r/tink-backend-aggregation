package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class SwedbankFallbackApiClient extends SwedbankDefaultApiClient {

    protected SwedbankFallbackApiClient(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            SwedbankStorage swedbankStorage,
            AgentComponentProvider componentProvider) {
        super(client, configuration, swedbankStorage, componentProvider);
    }
}
