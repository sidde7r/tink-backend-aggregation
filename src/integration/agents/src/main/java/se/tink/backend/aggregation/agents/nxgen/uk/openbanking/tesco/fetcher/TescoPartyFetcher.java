package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.tesco.fetcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.PartyDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.PartyFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class TescoPartyFetcher implements PartyFetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig config;
    private final PartyDataStorage storage;
    private final ScaExpirationValidator scaValidator;
    private static final long LIMIT_IN_MINUTES = 5;

    public TescoPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig aisConfig,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.config = aisConfig;
        this.storage = new PartyDataStorage(persistentStorage);
        this.scaValidator = new ScaExpirationValidator(persistentStorage, LIMIT_IN_MINUTES);
    }

    @Override
    public Optional<PartyV31Entity> fetchParty() {
        return Optional.empty();
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        if (config.isAccountPartyEndpointEnabled()) {
            if (scaValidator.isScaExpired()) {
                log.info(
                        "[FETCH ACCOUNT PARTIES] 5 minutes passed since last SCA. Restoring account parties from persistent storage.");
                return storage.restoreParties();
            }

            Optional<PartyV31Entity> party = apiClient.fetchV31Party(account.getAccountId());
            party.ifPresent(data -> storage.storeParties(Collections.singletonList(data)));
            return party.map(Collections::singletonList).orElse(Collections.emptyList());
        }

        return Collections.emptyList();
    }
}
