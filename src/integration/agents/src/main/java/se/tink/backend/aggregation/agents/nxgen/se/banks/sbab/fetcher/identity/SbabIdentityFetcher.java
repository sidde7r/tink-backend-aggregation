package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.fetcher.identity;

import se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.SbabApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class SbabIdentityFetcher implements IdentityDataFetcher {

    private final SbabApiClient apiClient;

    public SbabIdentityFetcher(SbabApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.getCustomer().toTinkIdentity();
    }
}
