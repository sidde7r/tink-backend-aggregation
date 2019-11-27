package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {
    private BigDecimal amount;
    private String currency;

    public ExactCurrencyAmount toAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }

    public ExactCurrencyAmount toAmount(CreditDebitIndicatorEntity creditDebitIndicator) {
        return CreditDebitIndicatorEntity.DBIT == creditDebitIndicator
                ? new ExactCurrencyAmount(amount.negate(), currency)
                : new ExactCurrencyAmount(amount, currency);
    }
}
