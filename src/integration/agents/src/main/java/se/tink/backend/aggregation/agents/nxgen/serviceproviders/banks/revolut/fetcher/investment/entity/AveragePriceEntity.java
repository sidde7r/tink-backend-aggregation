package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.revolut.fetcher.investment.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AveragePriceEntity {

    @JsonProperty("amount")
    private int amount;

    @JsonProperty("currency")
    private String currency;

    public int getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
