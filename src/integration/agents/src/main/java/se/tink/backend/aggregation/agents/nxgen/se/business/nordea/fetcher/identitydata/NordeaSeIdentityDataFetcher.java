package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.se.business.nordea.NordeaSEApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class NordeaSeIdentityDataFetcher implements IdentityDataFetcher {
    private final NordeaSEApiClient apiClient;

    public NordeaSeIdentityDataFetcher(final NordeaSEApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchIdentityData().toTinkIdentityData();
    }
}
