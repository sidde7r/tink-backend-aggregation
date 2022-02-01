package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.identity;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.apiclient.BredBanquePopulaireApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bredbanquepopulaire.fetcher.identity.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class BredBanquePopulaireIdentityDataFetcher implements IdentityDataFetcher {
    private final BredBanquePopulaireApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        return IdentityData.builder().setFullName(fetchConnectedPsu()).setDateOfBirth(null).build();
    }

    private String fetchConnectedPsu() {
        EndUserIdentityResponse endUserIdentityResponse = apiClient.getEndUserIdentity();
        return endUserIdentityResponse.getConnectedPsu();
    }
}
