package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.investments.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
    private String currency;
    private String value;

    public AmountEntity(String value, String currency) {
        this.value = value;
        this.currency = currency;
    }

    public String getCurrency() {
        return currency;
    }

    public String getValue() {
        return value;
    }
}
