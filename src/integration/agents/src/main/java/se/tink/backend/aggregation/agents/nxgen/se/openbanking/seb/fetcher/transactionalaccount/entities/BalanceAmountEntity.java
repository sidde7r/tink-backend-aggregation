package se.tink.backend.aggregation.agents.nxgen.se.openbanking.seb.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceAmountEntity {
    @JsonProperty
    private String currency;

    @JsonProperty
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return Double.parseDouble(amount);
    }
}
