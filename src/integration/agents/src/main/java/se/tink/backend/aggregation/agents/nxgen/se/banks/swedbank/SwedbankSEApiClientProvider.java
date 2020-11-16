package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank;

import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.SwedbankStorage;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.configuration.SwedbankConfiguration;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.interfaces.SwedbankApiClientProvider;
import se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.profile.SwedbankProfileSelector;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.AgentComponentProvider;
import se.tink.backend.aggregation.nxgen.http.client.TinkHttpClient;

public class SwedbankSEApiClientProvider implements SwedbankApiClientProvider {

    private final SwedbankProfileSelector profileSelector;

    public SwedbankSEApiClientProvider(SwedbankProfileSelector profileSelector) {
        this.profileSelector = profileSelector;
    }

    @Override
    public SwedbankSEApiClient createApiClient(
            TinkHttpClient client,
            SwedbankConfiguration configuration,
            SwedbankStorage swedbankStorage,
            AgentComponentProvider componentProvider) {
        return new SwedbankSEApiClient(
                client, configuration, swedbankStorage, profileSelector, componentProvider);
    }
}
