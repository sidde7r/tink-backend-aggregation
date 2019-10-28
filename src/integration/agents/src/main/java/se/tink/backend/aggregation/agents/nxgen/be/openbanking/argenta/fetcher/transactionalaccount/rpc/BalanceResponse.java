package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.fetcher.transactionalaccount.entity.BalancesEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceResponse {

    private List<BalancesEntity> balances;

    public List<BalancesEntity> getBalances() {
        return balances;
    }
}
