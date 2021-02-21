package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.bec.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {

    private String balanceType;
    private AmountEntity balanceAmount;

    public boolean isAvailable() {
        return balanceType.equalsIgnoreCase("expected");
    }

    public ExactCurrencyAmount getAmount() {
        return balanceAmount.toAmount();
    }
}
