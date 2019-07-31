package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities.Transactions;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class TransactionAmountEntity {

    private BigDecimal amount;

    private String currency;

    public ExactCurrencyAmount getAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }

    public String getCurrency() {
        return currency;
    }
}
