package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.societegenerale.fetcher.transactionalaccount.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionAmountEntity {

    private BigDecimal amount;

    private String currency;

    public ExactCurrencyAmount getAmount(CreditDebitIndicatorEntity creditDebitIndicator) {
        return CreditDebitIndicatorEntity.DBIT == creditDebitIndicator
                ? new ExactCurrencyAmount(amount.negate(), currency)
                : new ExactCurrencyAmount(amount, currency);
    }
}
