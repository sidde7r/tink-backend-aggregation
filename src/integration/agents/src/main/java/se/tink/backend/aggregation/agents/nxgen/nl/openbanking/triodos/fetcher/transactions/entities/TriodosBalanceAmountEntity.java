package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.triodos.fetcher.transactions.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TriodosBalanceAmountEntity {
    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public ExactCurrencyAmount toAmount(boolean isDebit) {
        if (isDebit) {
            return ExactCurrencyAmount.of(amount, currency);
        }
        return ExactCurrencyAmount.of(amount, currency).negate();
    }
}
