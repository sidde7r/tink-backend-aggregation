package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.accounts;

import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.BpceGroupConstants.Balance;
import se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.fetcher.transactionalaccount.entity.common.AmountEntity;
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
        return Balance.ACCOUNT_BALANCE.equalsIgnoreCase(balanceType);
    }
}
