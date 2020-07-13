package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.barclays.fetcher;

import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import se.tink.backend.agents.rpc.AccountTypes;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.AccountEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.base.entities.IdentityDataV31Entity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.PartyDataFetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.fetcher.PartyDataV31Fetcher;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v31.mapper.AccountTypeMapper;

@RequiredArgsConstructor
public class BarclaysPartyDataFetcher implements PartyDataFetcher {

    private final AccountTypeMapper accountTypeMapper;
    private final PartyDataV31Fetcher partyDataV31Fetcher;

    @Override
    public List<IdentityDataV31Entity> fetchAccountParties(AccountEntity accountEntity) {
        if (accountTypeMapper.getAccountType(accountEntity).equals(AccountTypes.CREDIT_CARD)) {
            // fetching parties is not supported for credit cards
            return Collections.emptyList();
        }
        return partyDataV31Fetcher.fetchAccountParties(accountEntity);
    }
}
