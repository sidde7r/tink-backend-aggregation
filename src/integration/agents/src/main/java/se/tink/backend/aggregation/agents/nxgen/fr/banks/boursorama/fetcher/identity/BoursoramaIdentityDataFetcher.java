package se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity;

import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.BoursoramaApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.boursorama.fetcher.identity.rpc.IdentityResponse;
import se.tink.libraries.identitydata.IdentityData;

public class BoursoramaIdentityDataFetcher {
    private final BoursoramaApiClient apiClient;

    public BoursoramaIdentityDataFetcher(BoursoramaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    public FetchIdentityDataResponse fetchIdentityData() {
        IdentityResponse identityResponse = this.apiClient.getIdentityData();
        IdentityData identityData =
                IdentityData.builder()
                        .addFirstNameElement(identityResponse.getFirstName())
                        .addSurnameElement(identityResponse.getLastName())
                        .setDateOfBirth(identityResponse.getBirthdayDate())
                        .build();

        return new FetchIdentityDataResponse(identityData);
    }
}
