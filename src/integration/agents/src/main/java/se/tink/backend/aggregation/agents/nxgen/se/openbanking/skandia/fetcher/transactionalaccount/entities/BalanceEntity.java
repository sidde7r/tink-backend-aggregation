package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    public static final Amount Default = Amount.inSEK(0);

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase("available");
    }

    public Amount toAmount() {
        return balanceAmount.toAmount();
    }
}
