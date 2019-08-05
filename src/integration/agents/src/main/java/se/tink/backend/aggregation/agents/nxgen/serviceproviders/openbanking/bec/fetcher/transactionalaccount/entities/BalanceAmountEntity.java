package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountEntity {

    private double amount;

    private String currency;

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    public void setCurrecyIfNull(String currency) {
        if (this.currency == null) {
            this.currency = currency;
        }
    }
}
