package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountBalance {

    private String balanceType;
    private boolean creditLimitIncluded;
    private BalanceAmountEntity balanceAmount;

    public ExactCurrencyAmount toAmount() {
        return balanceAmount.getAmount() != null
                ? balanceAmount.toAmount()
                : ExactCurrencyAmount.inEUR(0);
    }
}
