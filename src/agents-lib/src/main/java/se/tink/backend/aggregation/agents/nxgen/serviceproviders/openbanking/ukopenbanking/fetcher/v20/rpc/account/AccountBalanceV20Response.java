package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v20.rpc.account;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.v20.entities.account.AccountBalanceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;

public class AccountBalanceV20Response extends BaseResponse<List<AccountBalanceEntity>> {

    public Map<String, AccountBalanceEntity> toMap() {
        return getData().stream().collect(Collectors.toMap(AccountBalanceEntity::getAccountId, item -> item));
    }
}
