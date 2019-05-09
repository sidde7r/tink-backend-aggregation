package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.fetchers.identitydata;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.SwedbankDefaultApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.rpc.TouchResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

public class SwedbankIdentityDataFetcher implements IdentityDataFetcher {

    private final SwedbankDefaultApiClient apiClient;

    public SwedbankIdentityDataFetcher(SwedbankDefaultApiClient apiClient) {

        this.apiClient = apiClient;
    }

    @Override
    public IdentityData fetchIdentityData() {
        TouchResponse touchResponse = apiClient.touch();

        return SeIdentityData.of(
                touchResponse.getIdentifiedUserName(), touchResponse.getIdentifiedUserSsn());
    }

    public FetchIdentityDataResponse getIdentityDataResponse() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
