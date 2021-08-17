package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.virginmoney.fetcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.PartyDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyV31Fetcher;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class VirginMoneyPartyFetcher extends PartyV31Fetcher {

    private final UkOpenBankingApiClient apiClient;
    private final PartyDataStorage storage;
    private final ScaExpirationValidator scaValidator;
    private static final long LIMIT_IN_MINUTES = 5;

    public VirginMoneyPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage persistentStorage) {
        super(apiClient, config);
        this.apiClient = apiClient;
        this.storage = new PartyDataStorage(persistentStorage);
        this.scaValidator = new ScaExpirationValidator(persistentStorage, LIMIT_IN_MINUTES);
    }

    @Override
    public Optional<PartyV31Entity> fetchParty() {
        if (scaValidator.isScaExpired()) {
            log.info(
                    "[FETCH PARTY] 5 minutes passed since last SCA. Restoring party from persistent storage.");
            return storage.restoreParty();
        }
        Optional<PartyV31Entity> party = apiClient.fetchV31Party();
        party.ifPresent(storage::storeParty);
        return party;
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        if (scaValidator.isScaExpired()) {
            log.info(
                    "[FETCH ACCOUNT PARTY] 5 minutes passed since last SCA. Restoring account party from persistent storage.");
            return storage.restoreParties();
        }
        Optional<PartyV31Entity> party = apiClient.fetchV31Party(account.getAccountId());
        party.ifPresent(data -> storage.storeParties(Collections.singletonList(data)));
        return party.map(Collections::singletonList).orElse(Collections.emptyList());
    }
}
