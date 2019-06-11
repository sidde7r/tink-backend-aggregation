package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sibs.transactionalaccount.entity.account.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesResponse {

    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return balances;
    }
}
