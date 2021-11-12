package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {

    private String currency;
    private double value;

    public BalanceEntity(
            @JsonProperty("moneda") String currency, @JsonProperty("importe") double value) {
        this.currency = currency;
        this.value = value;
    }

    public ExactCurrencyAmount toExactCurrencyAmount() {
        return ExactCurrencyAmount.of(value, currency);
    }
}
