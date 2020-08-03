package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountEntity {

    private Double amount;
    private String currency;

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(BigDecimal.valueOf(amount), currency);
    }
}
