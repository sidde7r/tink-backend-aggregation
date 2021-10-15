package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.identity;

import lombok.AllArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.BankDataPersistentStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bankdata.fetcher.identity.rpc.IdentityResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.DkIdentityData;

@AllArgsConstructor
public class BankDataIdentityDataFetcher implements IdentityDataFetcher {
    private final BankDataApiClient apiClient;
    private final BankDataPersistentStorage jyskePersistentStorage;

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
