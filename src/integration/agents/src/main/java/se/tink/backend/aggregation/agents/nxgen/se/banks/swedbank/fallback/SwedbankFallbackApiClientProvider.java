package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.fallback;

import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.interfaces.SwedbankApiClientProvider;
import se.tink.backend.aggregation.configuration.agentsservice.AgentsServiceConfiguration;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class SwedbankFallbackApiClientProvider implements SwedbankApiClientProvider {

    private AgentsServiceConfiguration agentsServiceConfiguration;

    public SwedbankFallbackApiClientProvider(
            AgentsServiceConfiguration agentsServiceConfiguration) {
        this.agentsServiceConfiguration = agentsServiceConfiguration;
    }

    @Override
    public SwedbankFallbackApiClient getApiAgent(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            SwedbankStorage swedbankStorage,
            AgentComponentProvider componentProvider) {

        client.setEidasProxy(agentsServiceConfiguration.getEidasProxy());

        return new SwedbankFallbackApiClient(
                client,
                configuration,
                credentials.getField(Field.Key.USERNAME),
                swedbankStorage,
                componentProvider);
    }
}
