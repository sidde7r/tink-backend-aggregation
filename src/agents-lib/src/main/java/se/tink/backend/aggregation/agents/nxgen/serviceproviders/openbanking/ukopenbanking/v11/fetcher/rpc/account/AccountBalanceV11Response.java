package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.rpc.account;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.fetcher.rpc.BaseResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.v11.fetcher.entities.account.AccountBalanceEntity;

public class AccountBalanceV11Response extends BaseResponse<List<AccountBalanceEntity>> {

    public AccountBalanceEntity getBalance() {
        if (getData().size() == 1) {
            return getData().get(0);
        } else {
            throw new IllegalStateException("Balance response should only contain exactly one element.");
        }
    }
}
