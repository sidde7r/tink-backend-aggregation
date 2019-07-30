package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.berlingroup.fetcher.transactionalaccount.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountBaseEntity {
    private String currency;
    private BigDecimal amount;

    public String getCurrency() {
        return currency;
    }

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
