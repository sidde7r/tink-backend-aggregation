package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.SwedbankSEApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.interfaces.SwedbankApiClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class SwedbankDefaultApiClientProvider implements SwedbankApiClientProvider {
    @Override
    public SwedbankDefaultApiClient getApiAgent(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            Credentials credentials,
            SwedbankStorage swedbankStorage,
            AgentComponentProvider componentProvider) {
        return new SwedbankSEApiClient(
                client,
                configuration,
                credentials.getField(Field.Key.USERNAME),
                swedbankStorage,
                componentProvider);
    }
}
