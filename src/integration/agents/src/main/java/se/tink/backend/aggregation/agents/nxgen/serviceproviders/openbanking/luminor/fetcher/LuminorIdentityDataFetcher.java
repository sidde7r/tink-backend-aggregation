package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.LuminorConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class LuminorIdentityDataFetcher implements IdentityDataFetcher {
    private final PersistentStorage persistentStorage;

    @Override
    public IdentityData fetchIdentityData() {
        return persistentStorage.getOptional(StorageKeys.FULL_NAME).isPresent()
                ? IdentityData.builder()
                        .setFullName(persistentStorage.get(StorageKeys.FULL_NAME))
                        .setDateOfBirth(null)
                        .build()
                : IdentityData.builder().setFullName(null).setDateOfBirth(null).build();
    }
}
