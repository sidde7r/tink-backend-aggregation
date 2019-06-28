package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.executor.payment.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    private String value;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(String value, String currency) {
        this.value = value;
        this.currency = currency;
    }

    public static AmountEntity of(Amount amount) {
        return new AmountEntity(amount.getValue().toString(), amount.getCurrency());
    }

    public String getValue() {
        return value;
    }

    public String getCurrency() {
        return currency;
    }
}
