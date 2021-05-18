package se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.fetcher.identitydata;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.nordea.v33.NordeaFIApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class NordeaFIIdentityDataFetcher implements IdentityDataFetcher {
    private final NordeaFIApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchCustomerInfo().toIdentityData();
    }
}
