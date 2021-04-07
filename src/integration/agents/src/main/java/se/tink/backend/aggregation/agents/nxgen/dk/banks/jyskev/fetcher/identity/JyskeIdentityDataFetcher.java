package se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.identity;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.JyskeBankApiClient;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.JyskeBankPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.dk.banks.jyskev.fetcher.identity.rpc.IdentityResponse;
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

        return DkIdentityData.of(identityResponse.getName(), jyskePersistentStorage.getUserId());
    }
}
