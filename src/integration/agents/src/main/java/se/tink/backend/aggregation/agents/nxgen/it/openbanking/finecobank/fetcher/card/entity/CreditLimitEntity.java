package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class CreditLimitEntity {

    private BigDecimal amount;
    private String currency;

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }
}
