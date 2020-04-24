package se.tink.backend.aggregation.agents.nxgen.mx.banks.bbva.fetcher.loan.entity;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountsItem {
    private double amount;
    private String currency;

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, currency).negate();
    }
}
