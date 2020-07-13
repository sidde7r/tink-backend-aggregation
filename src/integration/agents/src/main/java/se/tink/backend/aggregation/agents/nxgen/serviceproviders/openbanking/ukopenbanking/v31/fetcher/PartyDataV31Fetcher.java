package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;

@RequiredArgsConstructor
public class PartyDataV31Fetcher implements PartyDataFetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig configuration;

    @Override
    public List<IdentityDataV31Entity> fetchAccountParties(AccountEntity accountEntity) {
        if (configuration.isAccountPartiesEndpointEnabled()) {
            return apiClient.fetchV31Parties(accountEntity.getAccountId());
        } else if (configuration.isAccountPartyEndpointEnabled()) {
            return apiClient
                    .fetchV31Party(accountEntity.getAccountId())
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }
        return Collections.emptyList();
    }
}
