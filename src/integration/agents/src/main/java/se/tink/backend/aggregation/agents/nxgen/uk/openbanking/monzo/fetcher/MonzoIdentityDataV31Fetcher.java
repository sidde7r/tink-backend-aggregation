package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.fetcher;

import java.time.LocalDateTime;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.IdentityDataMapper;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.monzo.MonzoConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class MonzoIdentityDataV31Fetcher implements IdentityDataFetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig configuration;
    private final IdentityDataMapper identityDataMapper;
    private final PersistentStorage persistentStorage;

    /**
     * * https://docs.monzo.com/#parties Endpoint /party expires 5 min after last SCA Identity data
     * will be saved and retrieved from the persistent storage to not enforce SCA on the user
     */
    @Override
    public Optional<IdentityData> fetchIdentityData() {
        if (isPartyEndpointDisabled()) {
            return Optional.empty();
        }

        if (isPartyEndpointExpired()) {
            return restoreIdentityData().map(identityDataMapper::map);
        }

        Optional<IdentityDataV31Entity> dataEntity = apiClient.fetchV31Party();
        dataEntity.ifPresent(this::storeIdentityData);

        return dataEntity.map(identityDataMapper::map);
    }

    private boolean isPartyEndpointDisabled() {
        return !configuration.isPartyEndpointEnabled();
    }

    private boolean isPartyEndpointExpired() {
        Optional<LocalDateTime> time = restoreRecentAuthenticationTime();
        return time.map(this::is5minPassed).orElse(Boolean.FALSE);
    }

    private boolean is5minPassed(LocalDateTime time) {
        // Time shortened to 4m 30s - just in case
        return LocalDateTime.now().isAfter(time.plusMinutes(4).plusSeconds(30));
    }

    private Optional<LocalDateTime> restoreRecentAuthenticationTime() {
        return persistentStorage
                .get(PersistentStorageKeys.LAST_SCA_TIME, String.class)
                .map(LocalDateTime::parse);
    }

    private Optional<IdentityDataV31Entity> restoreIdentityData() {
        return persistentStorage.get(StorageKeys.RECENT_IDENTITY_DATA, IdentityDataV31Entity.class);
    }

    private void storeIdentityData(IdentityDataV31Entity data) {
        persistentStorage.put(StorageKeys.RECENT_IDENTITY_DATA, data);
    }
}
