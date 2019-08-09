package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.fetcher.transactionalaccount.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bnpparibas.BnpParibasBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AmountEntity {

    private BigDecimal amount;

    private String currency;

    public BigDecimal getAmount() {
        return amount;
    }

    public String getCurrency() {
        return currency;
    }

    public ExactCurrencyAmount toAmount(String creditIndicator) {
        return (BnpParibasBaseConstants.ResponseValues.CREDIT_INDICATOR.equalsIgnoreCase(
                        creditIndicator))
                ? new ExactCurrencyAmount(amount, currency)
                : new ExactCurrencyAmount(amount.negate(), currency);
    }

    public ExactCurrencyAmount toAmount() {
        return new ExactCurrencyAmount(amount, currency);
    }
}
