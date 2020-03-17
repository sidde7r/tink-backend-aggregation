package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.interfaces.UkOpenBankingConstants.PartyEndpoints.IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.entities.identity.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.party.PartiesV31Response;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.rpc.party.PartyV31Response;
import se.tink.backend.aggregation.nxgen.http.url.URL;

@RequiredArgsConstructor
public class IdentityDataV31Fetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingV31AisConfiguration configuration;

    public Optional<IdentityDataV31Entity> fetchParty() {
        if (configuration.isPartyEndpointEnabled()) {
            return apiClient
                    .createAisRequest(new URL("/party"))
                    .get(PartyV31Response.class)
                    .getData();
        }
        return Optional.empty();
    }

    public Optional<List<IdentityDataV31Entity>> fetchAccountParties(String accountId) {
        if (configuration.isAccountPartiesEndpointEnabled()) {
            return apiClient
                    .createAisRequest(
                            new URL(
                                    String.format(
                                            IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTIES, accountId)))
                    .get(PartiesV31Response.class)
                    .getData();
        } else if (configuration.isAccountPartyEndpointEnabled()) {
            return apiClient
                    .createAisRequest(
                            new URL(
                                    String.format(
                                            IDENTITY_DATA_ENDPOINT_ACCOUNT_ID_PARTY, accountId)))
                    .get(PartyV31Response.class)
                    .getData()
                    .map(Collections::singletonList);
        }

        return Optional.empty();
    }
}
