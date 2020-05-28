package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.libraries.identitydata.IdentityData;

public class BoursoramaIdentityDataFetcher {
    private final BoursoramaApiClient apiClient;

    public BoursoramaIdentityDataFetcher(BoursoramaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        IdentityData identityData = this.apiClient.getIdentityData();
        return new FetchIdentityDataResponse(identityData);
    }
}
