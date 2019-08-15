package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.cards.entity;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesEntity {

    private String balanceType;
    private BalanceAmountEntity balanceAmount;

    public String getBalanceType() {
        return balanceType;
    }

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public boolean isForwardBalanceAvailable() {
        return !Strings.isNullOrEmpty(balanceType)
                && balanceType.equalsIgnoreCase(FinecoBankConstants.BalanceTypes.FORWARD_AVAILABLE);
    }

    public boolean isInterimBalanceAvailable() {
        return !Strings.isNullOrEmpty(balanceType)
                && balanceType.equalsIgnoreCase(FinecoBankConstants.BalanceTypes.INTERIM_AVAILABLE);
    }
}
