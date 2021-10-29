package se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.skandia.SkandiaConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(BalanceTypes.INTERIM_AVAILABLE);
    }

    public ExactCurrencyAmount toAmount() {
        return balanceAmount.toAmount();
    }
}
