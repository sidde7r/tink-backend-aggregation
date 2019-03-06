package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DisposedAmountEntity {
    private double amount;
    private CurrencyEntity currency;

    public double getAmount() {
        return amount;
    }

    public CurrencyEntity getCurrency() {
        return currency;
    }
}
