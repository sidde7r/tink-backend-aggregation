package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionAmountEntity {

    private String currency;

    private BigDecimal content;

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getContent() {
        return content;
    }

    public ExactCurrencyAmount getAmount() {
        return ExactCurrencyAmount.of(content, currency);
    }
}
