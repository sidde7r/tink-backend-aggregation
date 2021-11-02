package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.identity;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.apiclient.SocieteGeneraleApiClient;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.rpc.EndUserIdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class SocieteGeneraleIdentityDataFetcher implements IdentityDataFetcher {
    private final SocieteGeneraleApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        return IdentityData.builder().setFullName(fetchConnectedPsu()).setDateOfBirth(null).build();
    }

    private String fetchConnectedPsu() {
        EndUserIdentityResponse endUserIdentityResponse = apiClient.getEndUserIdentity();
        return endUserIdentityResponse.getConnectedPsu();
    }
}
