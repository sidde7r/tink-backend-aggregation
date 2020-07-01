package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.identity;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.LaBanquePostaleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.labanquepostale.fetcher.identity.dto.EndUserIdentityResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class LaBanquePostaleIdentityDataFetcher implements IdentityDataFetcher {

    private final LaBanquePostaleApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        final EndUserIdentityResponseDto endUserIdentityResponse = apiClient.getEndUserIdentity();
        return IdentityData.builder()
                .setFullName(endUserIdentityResponse.getConnectedPSU())
                .setDateOfBirth(null)
                .build();
    }

    public FetchIdentityDataResponse response() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }
}
