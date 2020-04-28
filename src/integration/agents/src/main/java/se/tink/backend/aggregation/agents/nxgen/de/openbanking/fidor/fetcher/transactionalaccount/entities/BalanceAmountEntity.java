package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountEntity {
    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public String getAmount() {
        return amount;
    }

    public ExactCurrencyAmount toAmount() {
        if (amount == null) {
            throw new IllegalStateException("Balance amonut is not available");
        }
        return ExactCurrencyAmount.of(Double.parseDouble(amount), currency);
    }
}
