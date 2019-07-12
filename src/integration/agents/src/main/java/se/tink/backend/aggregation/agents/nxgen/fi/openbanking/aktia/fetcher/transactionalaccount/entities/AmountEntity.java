package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.aktia.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountEntity {

    private String amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(Amount amount) {
        this.amount = String.valueOf(amount.getValue());
        this.currency = amount.getCurrency();
    }

    public Amount toAmount() {
        return new Amount(currency, amountToDouble());
    }

    private Double amountToDouble() {
        return Double.parseDouble(amount.replace(",", "."));
    }

    public String getCurrency() {
        return currency;
    }
}
