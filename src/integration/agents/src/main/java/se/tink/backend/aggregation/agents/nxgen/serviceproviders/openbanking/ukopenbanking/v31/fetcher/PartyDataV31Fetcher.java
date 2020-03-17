package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher;

import com.google.common.collect.ImmutableList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingAisConfig;

@RequiredArgsConstructor
public class PartyDataV31Fetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig configuration;

    public Optional<IdentityDataV31Entity> fetchParty() {
        if (configuration.isPartyEndpointEnabled()) {
            return apiClient.fetchV31Party();
        }
        return Optional.empty();
    }

    public List<IdentityDataV31Entity> fetchAccountParties(String accountId) {
        if (configuration.isAccountPartiesEndpointEnabled()) {
            return apiClient.fetchV31Parties(accountId);
        } else if (configuration.isAccountPartyEndpointEnabled()) {
            return apiClient
                    .fetchV31Party(accountId)
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }
        return ImmutableList.of();
    }
}
