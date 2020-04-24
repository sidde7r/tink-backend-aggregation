package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.fetcher.entities;

import java.math.BigDecimal;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class Balance {
    private int availableBalance;
    private String currency;

    public ExactCurrencyAmount getAvailableBalance() {
        return ExactCurrencyAmount.of(new BigDecimal(availableBalance), currency);
    }
}
