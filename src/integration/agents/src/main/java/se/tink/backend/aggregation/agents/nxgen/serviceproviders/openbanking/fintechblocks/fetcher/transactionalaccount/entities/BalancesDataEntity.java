package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.fintechblocks.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesDataEntity {

    @JsonProperty("Balance")
    private List<BalanceEntity> balance;

    public List<BalanceEntity> getBalance() {
        return balance;
    }
}
