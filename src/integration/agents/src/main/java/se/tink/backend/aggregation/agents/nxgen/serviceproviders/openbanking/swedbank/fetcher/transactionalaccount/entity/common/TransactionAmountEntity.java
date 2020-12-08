package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.swedbank.fetcher.transactionalaccount.entity.common;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionAmountEntity {
    private String currency;
    private BigDecimal amount;

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getAmount() {
        return amount;
    }

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }
}
