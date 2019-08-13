package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String currency;
    private BigDecimal amount;

    public ExactCurrencyAmount toTinkAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }
}
