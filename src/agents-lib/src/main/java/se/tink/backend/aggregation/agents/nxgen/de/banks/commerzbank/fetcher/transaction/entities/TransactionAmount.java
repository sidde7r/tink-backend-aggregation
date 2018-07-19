package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.transaction.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAmount {
    private Double value;
    private String currency;

    public Double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
