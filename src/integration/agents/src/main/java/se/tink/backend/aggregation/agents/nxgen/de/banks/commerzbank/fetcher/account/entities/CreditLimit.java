package se.tink.backend.aggregation.agents.nxgen.de.banks.commerzbank.fetcher.account.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class CreditLimit {
    private String value;
    private String currency;

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }

    public Amount toTinkAmount() {
        double limitValue = Double.parseDouble(value);
        return new Amount(currency, limitValue);
    }
}
