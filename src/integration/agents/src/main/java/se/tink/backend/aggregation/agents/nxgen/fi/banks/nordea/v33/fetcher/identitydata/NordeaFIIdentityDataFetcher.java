package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.identitydata;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

public class NordeaFIIdentityDataFetcher implements IdentityDataFetcher {
    private NordeaFIApiClient apiClient;

    public NordeaFIIdentityDataFetcher(NordeaFIApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchCustomerInfo().toIdentityData();
    }
}
