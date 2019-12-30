package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.boursorama.entity;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceResponse {

    private List<BalanceEntity> balances;

    public List<BalanceEntity> getBalances() {
        return balances;
    }
}
