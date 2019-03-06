package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class AmountsItem {
    private double amount;
    private String currency;

    public Amount getAmount() {
        return new Amount(currency, amount).negate();
    }
}
