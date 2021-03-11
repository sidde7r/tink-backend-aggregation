package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.PartyDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.ScaExpirationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class BarclaysPartyFetcher extends PartyV31Fetcher {

    private final UkOpenBankingApiClient apiClient;
    private final UkOpenBankingAisConfig config;
    private final AccountTypeMapper accountTypeMapper;
    private final PartyDataStorage storage;
    private final ScaExpirationValidator scaValidator;
    private static final long LIMIT_IN_MINUTES = 5;

    public BarclaysPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage persistentStorage) {
        super(apiClient, config);
        this.apiClient = apiClient;
        this.config = config;
        this.accountTypeMapper = new AccountTypeMapper(config);
        this.storage = new PartyDataStorage(persistentStorage);
        this.scaValidator = new ScaExpirationValidator(persistentStorage, LIMIT_IN_MINUTES);
    }

    public BarclaysPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PartyDataStorage storage,
            AccountTypeMapper accountTypeMapper,
            ScaExpirationValidator scaValidator) {
        super(apiClient, config);
        this.apiClient = apiClient;
        this.config = config;
        this.accountTypeMapper = accountTypeMapper;
        this.storage = storage;
        this.scaValidator = scaValidator;
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        // according to the docs parties data is not available for business accounts and barclaycard
        // https://developer.barclays.com/apis/account-and-transactions/overview#accordion-section-0
        if (isCreditCard(account) || isBusinessAccount(account)) {
            return Collections.emptyList();
        }

        if (config.isAccountPartiesEndpointEnabled()) {
            if (scaValidator.isScaExpired()) {
                return storage.restoreParties();
            }

            List<PartyV31Entity> parties = apiClient.fetchV31Parties(account.getAccountId());
            storage.storeParties(parties);
            return parties;
        }

        if (config.isAccountPartyEndpointEnabled()) {
            if (scaValidator.isScaExpired()) {
                return storage.restoreParties();
            }

            Optional<PartyV31Entity> party = apiClient.fetchV31Party(account.getAccountId());
            party.ifPresent(data -> storage.storeParties(Collections.singletonList(data)));
            return party.map(Collections::singletonList).orElse(Collections.emptyList());
        }

        return Collections.emptyList();
    }

    private boolean isCreditCard(AccountEntity account) {
        return accountTypeMapper.getAccountType(account).equals(AccountTypes.CREDIT_CARD);
    }

    private boolean isBusinessAccount(AccountEntity account) {
        return accountTypeMapper
                .getAccountOwnershipType(account)
                .equals(AccountOwnershipType.BUSINESS);
    }
}
