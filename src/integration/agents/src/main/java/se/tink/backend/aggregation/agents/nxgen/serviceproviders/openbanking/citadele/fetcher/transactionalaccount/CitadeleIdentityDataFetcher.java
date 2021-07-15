package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.fetcher.transactionalaccount;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.citadele.CitadeleBaseConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class CitadeleIdentityDataFetcher implements IdentityDataFetcher {
    private final PersistentStorage persistentStorage;

    @Override
    public IdentityData fetchIdentityData() {
        return IdentityData.builder()
                .setFullName(persistentStorage.getOptional(StorageKeys.HOLDER_NAME).orElse(null))
                .setDateOfBirth(null)
                .build();
    }
}
