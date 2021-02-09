package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.fetcher.transactionalaccount.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    @JsonProperty("Amount")
    private BigDecimal amount;

    @JsonProperty("AmountWithoutDecimals")
    private int amountWithoutDecimals;

    @JsonProperty("DisposableAmount")
    private BigDecimal disposableAmount;

    @JsonProperty("DisposableAmountWithoutDecimals")
    private int disposableAmountWithoutDecimals;

    public ExactCurrencyAmount getAmount(String currency) {
        return ExactCurrencyAmount.of(amount, currency);
    }

    public ExactCurrencyAmount getDisposableAmount(String currency) {
        return ExactCurrencyAmount.of(disposableAmount, currency);
    }
}
