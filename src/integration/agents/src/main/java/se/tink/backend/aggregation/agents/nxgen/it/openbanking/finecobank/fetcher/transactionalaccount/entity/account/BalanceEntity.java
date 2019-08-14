package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.account;

import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.BalanceTypes;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private AmountEntity balanceAmount;
    private String balanceType;
    private String referenceDate;
    private Boolean creditLimitIncluded;

    public AmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public boolean isForwardBalanceAvailable() {
        return BalanceTypes.FORWARD_AVAILABLE.equalsIgnoreCase(balanceType);
    }

    public boolean isInterimBalanceAvailable() {
        return BalanceTypes.INTERIM_AVAILABLE.equalsIgnoreCase(balanceType);
    }
}
