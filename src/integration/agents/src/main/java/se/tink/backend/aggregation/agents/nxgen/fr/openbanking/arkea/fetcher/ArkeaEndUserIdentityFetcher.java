package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.fetcher;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.arkea.apiclient.ArkeaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class ArkeaEndUserIdentityFetcher implements IdentityDataFetcher {

    private final ArkeaApiClient apiClient;

    @Override
    public IdentityData fetchIdentityData() {
        return IdentityData.builder()
                .setFullName(apiClient.getUserIdentity().getConnectedPsu())
                .setDateOfBirth(null)
                .build();
    }
}
