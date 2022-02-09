package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.ukob.barclays.fetcher;

import java.util.Collections;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.UkOpenBankingApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.PartyV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.interfaces.UkOpenBankingAisConfig;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@Slf4j
public class BarclaysPartyFetcher extends PartyV31Fetcher {

    public BarclaysPartyFetcher(
            UkOpenBankingApiClient apiClient,
            UkOpenBankingAisConfig config,
            PersistentStorage persistentStorage) {
        super(apiClient, config, persistentStorage);
    }

    @Override
    public List<PartyV31Entity> fetchAccountParties(AccountEntity account) {
        // according to the docs parties data is not available for business accounts and barclaycard
        // https://developer.barclays.com/apis/account-and-transactions/overview#accordion-section-0
        if (isNotPossibleToFetchParties(account)) {
            return Collections.emptyList();
        }
        return super.fetchAccountParties(account);
    }

    private boolean isNotPossibleToFetchParties(AccountEntity account) {
        if (account.getRawAccountType() == null) {
            return true;
        }
        return (isCreditCard(account) || isBusinessAccount(account));
    }

    private boolean isCreditCard(AccountEntity account) {
        return AccountTypeMapper.getAccountType(account).equals(AccountTypes.CREDIT_CARD);
    }

    private boolean isBusinessAccount(AccountEntity account) {
        return account.getRawAccountType().equals("Business");
    }
}
