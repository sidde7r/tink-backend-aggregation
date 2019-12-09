package se.tink.backend.aggregation.agents.nxgen.no.openbanking.dnb.fetcher.entity;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountBaseEntity {
    private String currency;
    private BigDecimal amount;

    public ExactCurrencyAmount toAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }
}
