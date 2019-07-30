package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private BigDecimal amount;
    private String currency;

    public AmountEntity() {}

    public AmountEntity(ExactCurrencyAmount amount) {
        this.amount = amount.getExactValue();
        this.currency = amount.getCurrencyCode();
    }

    @JsonIgnore
    public ExactCurrencyAmount toAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }
}
