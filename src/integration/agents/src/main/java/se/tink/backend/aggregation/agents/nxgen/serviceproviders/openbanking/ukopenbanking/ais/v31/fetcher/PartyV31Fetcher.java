package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;

public class PartyV31Fetcher implements PartyFetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig config;

    public PartyV31Fetcher(UkOpenBankingApiClient apiClient, UkOpenBankingAisConfig config) {
        this.apiClient = apiClient;
        this.config = config;
    }

    @Override
    public Optional<PartyV31Entity> fetchParty() {
        if (config.isPartyEndpointEnabled()) {
            return apiClient.fetchV31Party();
        }
        return Optional.empty();
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        List<PartyV31Entity> parties = Collections.emptyList();

        /**
         * https://openbankinguk.github.io/read-write-api-site3/v3.1.7/resources-and-data-models/aisp/Parties.html#get-accounts-accountid-parties
         * if endpoint available, it MAY return details on the account owner(s)/holder(s) and
         * operator(s)
         */
        if (config.isAccountPartiesEndpointEnabled()) {
            parties = apiClient.fetchV31Parties(account.getAccountId());
        }

        /**
         * https://openbankinguk.github.io/read-write-api-site3/v3.1.7/resources-and-data-models/aisp/Parties.html#get-accounts-accountid-party
         * if endpoint available, it MUST return details on the account owner/holder
         */
        if (parties.isEmpty() && config.isAccountPartyEndpointEnabled()) {
            return apiClient
                    .fetchV31Party(account.getAccountId())
                    .map(Collections::singletonList)
                    .orElse(Collections.emptyList());
        }

        return parties;
    }
}
