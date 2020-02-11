package se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.rpc;

import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.Nulls;
import java.util.Collection;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.AccountIbanEntity;
import se.tink.backend.aggregation.agents.nxgen.de.openbanking.sparkassen.fetcher.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchBalancesResponse {
    private AccountIbanEntity account;
    private Collection<BalanceEntity> balances;

    @JsonSetter(nulls = Nulls.AS_EMPTY)
    public void setBalances(Collection<BalanceEntity> balances) {
        this.balances = balances;
    }

    public Collection<BalanceEntity> getBalances() {
        return balances;
    }
}
