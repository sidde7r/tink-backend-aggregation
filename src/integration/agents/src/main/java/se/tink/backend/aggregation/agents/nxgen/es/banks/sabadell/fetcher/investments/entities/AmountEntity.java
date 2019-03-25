package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private String currency;
    private String value;

    public AmountEntity() {}

    public AmountEntity(String value, String currency) {
        this.value = value;
        this.currency = currency;
    }

    public Amount toTinkAmount() {
        return new Amount(currency, Long.valueOf(value), 2);
    }

    public String getCurrency() {
        return currency;
    }

    public String getValue() {
        return value;
    }
}
