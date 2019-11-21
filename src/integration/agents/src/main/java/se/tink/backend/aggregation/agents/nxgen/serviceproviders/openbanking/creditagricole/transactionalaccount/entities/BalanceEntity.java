package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.transactionalaccount.entities;

import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.CreditAgricoleBaseConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private AmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String lastCommitedTransaction;
    private String name;
    private String referenceDate;

    public ExactCurrencyAmount toAmount() {
        return balanceAmount.toAmount();
    }

    public boolean isAvailablebalance() {
        return balanceType.equalsIgnoreCase(BalanceTypes.CLBD)
                || balanceType.equalsIgnoreCase(BalanceTypes.XPCD);
    }
}
