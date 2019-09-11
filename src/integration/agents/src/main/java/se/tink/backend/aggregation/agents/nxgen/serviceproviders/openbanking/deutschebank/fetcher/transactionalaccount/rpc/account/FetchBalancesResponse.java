package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.rpc.account;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.deutschebank.fetcher.transactionalaccount.entity.account.BalanceBaseEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchBalancesResponse {

    private List<BalanceBaseEntity> balances;

    public List<BalanceBaseEntity> getBalances() {
        return balances;
    }
}
