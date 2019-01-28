package se.tink.backend.aggregation.agents.nxgen.es.banks.openbank.fetcher.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    @JsonProperty("divisa")
    private String currency;

    @JsonProperty("importe")
    private double amount;

    public String getCurrency() {
        return currency;
    }

    public double getAmount() {
        return amount;
    }

    public Amount toTinkAmount() {
        return new Amount(getCurrency(), getAmount());
    }
}
