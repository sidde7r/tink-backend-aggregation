package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionAmountEntity {

    private BigDecimal amount;
    private String currency;

    public BigDecimal getNegatedAmount() {
        return amount.negate();
    }

    public String getCurrency() {
        return currency;
    }

    public ExactCurrencyAmount getTinkAmount() {
        return new ExactCurrencyAmount(getNegatedAmount(), currency);
    }
}
