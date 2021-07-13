package se.tink.backend.aggregation.agents.nxgen.es.banks.imaginbank.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    @JsonProperty("moneda")
    private String currency;

    @JsonProperty("importe")
    private BigDecimal amount;

    @JsonIgnore
    public ExactCurrencyAmount toExactCurrencyAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }

    @JsonIgnore
    public String getCurrency() {
        return currency;
    }

    @JsonIgnore
    public BigDecimal getAmount() {
        return amount == null ? BigDecimal.ZERO : amount;
    }
}
