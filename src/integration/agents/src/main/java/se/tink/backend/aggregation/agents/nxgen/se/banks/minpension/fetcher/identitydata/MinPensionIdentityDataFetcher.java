package se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.fetcher.identitydata;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.banks.minpension.MinPensionConstants.StorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.refresh.identitydata.IdentityDataFetcher;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.identitydata.IdentityData;
import se.tink.libraries.identitydata.countries.SeIdentityData;

@RequiredArgsConstructor
public class MinPensionIdentityDataFetcher implements IdentityDataFetcher {
    private final MinPensionApiClient minPensionApiClient;
    private final SessionStorage sessionStorage;

    @Override
    public IdentityData fetchIdentityData() {
        return SeIdentityData.of(
                sessionStorage.get(StorageKeys.FIRST_NAME),
                sessionStorage.get(StorageKeys.LAST_NAME),
                minPensionApiClient.fetchSsn());
    }
}
