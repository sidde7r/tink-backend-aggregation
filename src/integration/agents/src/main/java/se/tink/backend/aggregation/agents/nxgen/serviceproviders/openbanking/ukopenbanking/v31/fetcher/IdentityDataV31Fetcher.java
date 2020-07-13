package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.IdentityDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.IdentityDataMapper;
import se.tink.libraries.identitydata.IdentityData;

@RequiredArgsConstructor
public class IdentityDataV31Fetcher implements IdentityDataFetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig configuration;
    private final IdentityDataMapper identityDataMapper;

    @Override
    public Optional<IdentityData> fetchIdentityData() {
        if (configuration.isPartyEndpointEnabled()) {
            return apiClient.fetchV31Party().map(identityDataMapper::map);
        }
        return Optional.empty();
    }
}
