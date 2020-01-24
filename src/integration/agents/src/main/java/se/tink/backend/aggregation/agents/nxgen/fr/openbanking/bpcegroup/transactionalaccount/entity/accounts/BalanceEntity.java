package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.accounts;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.libraries.amount.ExactCurrencyAmount;

@JsonObject
public class BalanceEntity {
    private String balanceType;
    private String name;
    private AmountEntity balanceAmount;
    private String referenceDate;

    public ExactCurrencyAmount getTinkAmount() {
        return balanceAmount.toTinkAmount();
    }

    public boolean isBalanceType() {
        return BalanceType.ACCOUNT.getType().equalsIgnoreCase(balanceType);
    }
}
