package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v11.rpc.account;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v11.entities.account.AccountBalanceEntity;

public class AccountBalanceV11Response extends BaseResponse<List<AccountBalanceEntity>> {

    public Map<String, AccountBalanceEntity> toMap() {
        return getData().stream().collect(Collectors.toMap(AccountBalanceEntity::getAccountId, item -> item));
    }
}
