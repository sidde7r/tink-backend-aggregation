package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bankverlag.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.List;
import se.tink.backend.aggregation.agents.utils.berlingroup.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchBalancesResponse {
    private List<BalanceEntity> balances;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setBalances(List<BalanceEntity> balances) {
        this.balances = balances;
    }

    public List<BalanceEntity> getBalances() {
        return balances;
    }
}
