package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.fetchers.credit.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceDataEntity {
    @JsonProperty("Balance")
    private List<BalanceEntity> balance;

    public List<BalanceEntity> getBalance() {
        return balance;
    }
}
