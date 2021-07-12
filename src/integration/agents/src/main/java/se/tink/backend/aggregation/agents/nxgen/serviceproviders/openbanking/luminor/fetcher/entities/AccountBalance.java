package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.luminor.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class AccountBalance {

    private String balanceType;
    private boolean creditLimitIncluded;
    private BalanceAmountEntity balanceAmount;

    public ExactCurrencyAmount toAmount() {
        if (balanceAmount == null) {
            throw new IllegalStateException("Balance amonut is not available");
        }
        return balanceAmount.toAmount();
    }
}
