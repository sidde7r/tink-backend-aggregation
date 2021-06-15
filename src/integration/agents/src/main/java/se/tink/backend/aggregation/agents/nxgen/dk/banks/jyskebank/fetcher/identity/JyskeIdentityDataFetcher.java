package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.identity;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.JyskeBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskebank.fetcher.identity.rpc.IdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.DkIdentityData;

@AllArgsConstructor
public class JyskeIdentityDataFetcher implements IdentityDataFetcher {
    private final JyskeBankApiClient apiClient;
    private final JyskeBankPersistentStorage jyskePersistentStorage;

    @Override
    public IdentityData fetchIdentityData() {
        final IdentityResponse identityResponse = apiClient.fetchIdentityData();
        final String userId = jyskePersistentStorage.getUserId();

        if (DkIdentityData.isCpr(userId)) {
            return DkIdentityData.of(identityResponse.getName(), userId);
        }
        return IdentityData.builder()
                .setFullName(identityResponse.getName())
                .setDateOfBirth(null)
                .build();
    }
}
