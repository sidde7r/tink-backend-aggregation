package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.rpc;

import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fiducia.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class GetBalancesResponse {

    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return balances;
    }
}
