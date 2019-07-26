package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountEntity {

    @JsonProperty("amount")
    private double amount;

    @JsonProperty("currency")
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
