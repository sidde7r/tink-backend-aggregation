package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.AccountOwnershipType;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.fetcher.PartyDataV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.v31.mapper.AccountTypeMapper;

@RequiredArgsConstructor
public class BarclaysPartyDataFetcher implements PartyDataFetcher {

    private final AccountTypeMapper accountTypeMapper;
    private final PartyDataV31Fetcher partyDataV31Fetcher;

    @Override
    public List<IdentityDataV31Entity> fetchAccountParties(AccountEntity account) {
        // according to the docs parties data is not available for business accounts and barclaycard
        // https://developer.barclays.com/apis/account-and-transactions/overview#accordion-section-0
        if (isCreditCard(account) || isBusinessAccount(account)) {
            return Collections.emptyList();
        }
        return partyDataV31Fetcher.fetchAccountParties(account);
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
