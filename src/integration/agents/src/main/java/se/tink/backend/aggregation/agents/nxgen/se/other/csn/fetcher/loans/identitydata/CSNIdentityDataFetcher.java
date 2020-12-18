package se.tink.backend.aggregation.agents.nxgen.se.other.csn.fetcher.loans.identitydata;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class CSNIdentityDataFetcher implements IdentityDataFetcher {
    private final CSNApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        return apiClient.fetchUserInfo().getIdentityData();
    }
}
