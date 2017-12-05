package se.tink.backend.aggregation.agents.nxgen.es.banks.sabadell.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AmountEntity {
    private String value;
    private String currency;

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
