package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.interfaces.SwedbankApiClientProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class SwedbankSEApiClientProvider implements SwedbankApiClientProvider {

    @Override
    public SwedbankSEApiClient getApiAgent(
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
