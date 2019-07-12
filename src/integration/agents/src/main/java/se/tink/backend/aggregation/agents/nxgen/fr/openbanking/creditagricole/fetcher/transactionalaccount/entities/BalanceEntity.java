package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.fetcher.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.creditagricole.CreditAgricoleConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.Amount;

@JsonObject
public class BalanceEntity {
    private AmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String lastCommitedTransaction;
    private String name;
    private String referenceDate;

    public Amount toAmount() {
        return balanceAmount.toAmount();
    }

    public boolean isAvailablebalance() {
        return balanceType.equalsIgnoreCase(BalanceTypes.CLBD)
                || balanceType.equalsIgnoreCase(BalanceTypes.XPCD);
    }
}
