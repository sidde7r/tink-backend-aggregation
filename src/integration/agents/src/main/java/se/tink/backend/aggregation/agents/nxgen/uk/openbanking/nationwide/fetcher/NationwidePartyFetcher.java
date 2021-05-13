package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.nationwide.fetcher;

import java.util.Collections;
import java.util.List;
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
public class NationwidePartyFetcher extends PartyV31Fetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig config;
    private final PartyDataStorage storage;
    private final ScaExpirationValidator scaValidator;
    private static final long LIMIT_IN_MINUTES = 5;

    public NationwidePartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage storage) {
        super(apiClient, config);
        this.apiClient = apiClient;
        this.config = config;
        this.storage = new PartyDataStorage(storage);
        this.scaValidator = new ScaExpirationValidator(storage, LIMIT_IN_MINUTES);
    }

    public NationwidePartyFetcher(
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
        if (config.isAccountPartiesEndpointEnabled()) {
            if (scaValidator.isScaExpired()) {
                log.info(
                        "[FETCH ACCOUNT PARTIES] 5 minutes passed since last SCA. Restoring account parties from persistent storage.");
                return storage.restoreParties();
            }

            List<PartyV31Entity> parties = apiClient.fetchV31Parties(account.getAccountId());
            storage.storeParties(parties);
            return parties;
        }

        return Collections.emptyList();
    }
}
