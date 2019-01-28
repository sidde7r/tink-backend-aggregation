package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.samlink.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {
    private double amount;
    private String currency;

    public double getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public Amount toTinkAmount() {
        return new Amount(currency, amount);
    }
}
