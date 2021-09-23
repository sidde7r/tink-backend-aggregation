package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class BarclaysPartyFetcher extends PartyV31Fetcher {
    private final AccountTypeMapper accountTypeMapper;

    public BarclaysPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            AccountTypeMapper accountTypeMapper,
            PersistentStorage persistentStorage) {
        super(apiClient, config, persistentStorage);
        this.accountTypeMapper = accountTypeMapper;
    }

    public BarclaysPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage storage,
            AccountTypeMapper accountTypeMapper) {
        super(apiClient, config, storage);
        this.accountTypeMapper = accountTypeMapper;
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        // according to the docs parties data is not available for business accounts and barclaycard
        // https://developer.barclays.com/apis/account-and-transactions/overview#accordion-section-0
        if (isCreditCard(account) || isBusinessAccount(account)) {
            return Collections.emptyList();
        }
        return super.fetchAccountParties(account);
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
