package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.hsbc.fetcher;

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
public class HsbcPartyFetcher extends PartyV31Fetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig config;
    private final PartyDataStorage storage;
    private final ScaExpirationValidator scaValidator;
    private static final long LIMIT_IN_MINUTES = 60;

    public HsbcPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage persistentStorage) {
        super(apiClient, config);
        this.apiClient = apiClient;
        this.config = config;
        this.storage = new PartyDataStorage(persistentStorage);
        this.scaValidator = new ScaExpirationValidator(persistentStorage, LIMIT_IN_MINUTES);
    }

    public HsbcPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PartyDataStorage storage,
            ScaExpirationValidator scaValidator) {
        super(apiClient, config);
        this.apiClient = apiClient;
        this.config = config;
        this.storage = storage;
        this.scaValidator = scaValidator;
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        if (config.isAccountPartyEndpointEnabled()) {
            if (scaValidator.isScaExpired()) {
                log.info(
                        "[FETCH ACCOUNT PARTY] 5 minutes passed since last SCA. "
                                + "Restoring account party from persistent storage.");
                return storage.restoreParties();
            }

            Optional<PartyV31Entity> party = apiClient.fetchV31Party(account.getAccountId());
            party.ifPresent(data -> storage.storeParties(Collections.singletonList(data)));
            return party.map(Collections::singletonList).orElse(Collections.emptyList());
        }

        return Collections.emptyList();
    }
}
