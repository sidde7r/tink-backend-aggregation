package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    @JsonProperty("quantity")
    private String quantity;

    @JsonProperty("type")
    private String type;

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("currency")
    private String currency;

    public String getQuantity() {
        return quantity;
    }

    public String getType() {
        return type;
    }

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
