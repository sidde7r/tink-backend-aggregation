package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.account;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private String currency;
    private BigDecimal amount;

    public ExactCurrencyAmount toTinkAmount() {
        return ExactCurrencyAmount.of(amount, currency);
    }
}
