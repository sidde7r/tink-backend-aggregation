package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher;

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
public class PartyV31Fetcher implements PartyFetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig config;
    private final PartyDataStorage storage;
    private final ScaExpirationValidator scaValidator;
    private static final long DEFAULT_LIMIT_IN_MINUTES = 5;

    public PartyV31Fetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage persistentStorage,
            long limitInMinutes) {
        this.apiClient = apiClient;
        this.config = config;
        storage = new PartyDataStorage(persistentStorage);
        scaValidator = new ScaExpirationValidator(persistentStorage, limitInMinutes);
    }

    public PartyV31Fetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.config = config;
        this.storage = new PartyDataStorage(persistentStorage);
        this.scaValidator = new ScaExpirationValidator(persistentStorage, DEFAULT_LIMIT_IN_MINUTES);
    }

    @Override
    public Optional<PartyV31Entity> fetchParty() {
        if (config.isPartyEndpointEnabled()) {
            if (scaValidator.isScaExpired()) {
                log.info(
                        "[FETCH PARTY] "
                                + scaValidator.getLimitInMinutes()
                                + " minutes passed since last SCA. Restoring party from persistent storage.");
                return storage.restoreParty();
            }

            Optional<PartyV31Entity> party = apiClient.fetchAccountParty();
            party.ifPresent(storage::storeParty);
            return party;
        }

        return Optional.empty();
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        List<PartyV31Entity> parties = Collections.emptyList();

        if (scaValidator.isScaExpired()) {
            log.info(
                    "[FETCH ACCOUNT PARTY] "
                            + scaValidator.getLimitInMinutes()
                            + " minutes passed since last SCA. Restoring account party / parties from persistent storage.");
            return storage.restoreParties();
        }

        /*
         https://openbankinguk.github.io/read-write-api-site3/v3.1.7/resources-and-data-models/aisp/Parties.html#get-accounts-accountid-parties
         if endpoint available, it MAY return details on the account owner(s)/holder(s) and
         operator(s)
        */
        if (config.isAccountPartiesEndpointEnabled()) {
            parties = apiClient.fetchAccountParties(account.getAccountId());
        }

        /*
         * https://openbankinguk.github.io/read-write-api-site3/v3.1.7/resources-and-data-models/aisp/Parties.html#get-accounts-accountid-party
         * if endpoint available, it MUST return details on the account owner/holder
         */
        if (parties.isEmpty() && config.isAccountPartyEndpointEnabled()) {
            parties =
                    apiClient
                            .fetchAccountParty(account.getAccountId())
                            .map(Collections::singletonList)
                            .orElse(Collections.emptyList());
        }
        storage.storeParties(parties);
        return parties;
    }
}
