package se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.crelan.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetBalanceResponse {
    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return balances;
    }
}
