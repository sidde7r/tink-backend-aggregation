package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase.fetcher.cardaccounts.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TransactionAmountEntity {

    private BigDecimal amount;
    private String currency;

    public BigDecimal getAmount() {
        return amount.negate();
    }

    public String getCurrency() {
        return currency;
    }
}
