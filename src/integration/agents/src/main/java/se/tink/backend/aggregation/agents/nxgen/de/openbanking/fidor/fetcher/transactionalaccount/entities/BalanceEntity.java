package se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.de.openbanking.fidor.FidorConstants.Accounts;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private String balanceType;
    private BalanceAmountEntity balanceAmount;

    public ExactCurrencyAmount toAmount() {
        return balanceAmount.getAmount() != null
                ? balanceAmount.toAmount()
                : ExactCurrencyAmount.inDKK(0);
    }

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase(Accounts.AVAILABLE_BALANCE);
    }
}
