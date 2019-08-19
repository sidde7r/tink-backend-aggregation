package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sparebank.fetcher.transactionalaccount.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceAmountEntity {
    private String currency;
    private String amount;

    public String getCurrency() {
        return currency;
    }

    public ExactCurrencyAmount toAmount() {
        return ExactCurrencyAmount.of(new BigDecimal(amount), currency);
    }
}
