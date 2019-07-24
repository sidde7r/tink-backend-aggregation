package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.entities.AccountReferenceEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.redsys.fetcher.transactionalaccount.entities.BalanceEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AccountBalancesResponse {
    @JsonProperty private AccountReferenceEntity account;
    @JsonProperty private List<BalanceEntity> balances;

    @JsonIgnore
    public List<BalanceEntity> getBalances() {
        return balances;
    }
}
