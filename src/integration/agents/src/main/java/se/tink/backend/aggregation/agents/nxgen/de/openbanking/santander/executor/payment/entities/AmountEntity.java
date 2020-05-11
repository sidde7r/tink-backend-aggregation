package se.tink.backend.aggregation.agents.nxgen.de.openbanking.santander.executor.payment.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String amount;
    private String currency;

    @JsonIgnore
    public AmountEntity(String amount, String currency) {
        this.amount = amount;
        this.currency = currency;
    }

    public AmountEntity() {}

    @JsonIgnore
    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    public String getCurrency() {
        return currency;
    }
}
