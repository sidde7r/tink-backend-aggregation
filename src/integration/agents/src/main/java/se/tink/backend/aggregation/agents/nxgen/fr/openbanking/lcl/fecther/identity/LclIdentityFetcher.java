package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.fecther.identity;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.FetchIdentityDataResponse;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.LclApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.apiclient.dto.identity.EndUserIdentityResponseDto;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class LclIdentityFetcher implements IdentityDataFetcher {

    private final LclApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        final EndUserIdentityResponseDto response = apiClient.getEndUserIdentity();

        return convertResponseToIdentityData(response);
    }

    public FetchIdentityDataResponse response() {
        return new FetchIdentityDataResponse(fetchIdentityData());
    }

    private static IdentityData convertResponseToIdentityData(EndUserIdentityResponseDto response) {
        return IdentityData.builder()
                .setFullName(response.getConnectedPsu())
                .setDateOfBirth(null)
                .build();
    }
}
