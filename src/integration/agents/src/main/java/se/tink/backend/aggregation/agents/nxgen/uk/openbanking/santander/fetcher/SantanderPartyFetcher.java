package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.santander.fetcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.PartyDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class SantanderPartyFetcher implements PartyFetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig config;
    private final PartyDataStorage storage;
    private final ScaExpirationValidator scaValidator;
    private static final long LIMIT_IN_MINUTES = 5;

    public SantanderPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.config = config;
        this.storage = new PartyDataStorage(persistentStorage);
        this.scaValidator = new ScaExpirationValidator(persistentStorage, LIMIT_IN_MINUTES);
    }

    @Override
    public Optional<PartyV31Entity> fetchParty() {
        if (isPartyEndpointDisabled()) {
            return Optional.empty();
        }

        if (scaValidator.isScaExpired()) {
            return storage.restoreParty();
        }

        Optional<PartyV31Entity> party = apiClient.fetchV31Party();
        party.ifPresent(storage::storeParty);

        return party;
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        List<PartyV31Entity> parties = Collections.emptyList();

        if (config.isAccountPartiesEndpointEnabled()) {
            if (scaValidator.isScaExpired()) {
                return storage.restoreParties();
            }

            parties = apiClient.fetchV31Parties(account.getAccountId());
            storage.storeParties(parties);
        }

        if (parties.isEmpty() && config.isAccountPartyEndpointEnabled()) {
            if (scaValidator.isScaExpired()) {
                return storage.restoreParties();
            }

            Optional<PartyV31Entity> party = apiClient.fetchV31Party(account.getAccountId());
            party.ifPresent(data -> storage.storeParties(Collections.singletonList(data)));
            return party.map(Collections::singletonList).orElse(Collections.emptyList());
        }

        return parties;
    }

    private boolean isPartyEndpointDisabled() {
        return !config.isPartyEndpointEnabled();
    }
}
