package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.core.Amount;

@JsonObject
public class BalanceEntity {
    private Double value;
    private String currency;

    public Double getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
