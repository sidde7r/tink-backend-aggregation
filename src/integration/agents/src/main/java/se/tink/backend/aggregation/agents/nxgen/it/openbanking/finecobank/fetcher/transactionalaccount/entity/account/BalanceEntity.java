package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.transactionalaccount.entity.account;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants;
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
        return !Strings.isNullOrEmpty(balanceType)
                && balanceType.equalsIgnoreCase(FinecoBankConstants.BalanceTypes.FORWARD_AVAILABLE);
    }

    public boolean isInterimBalanceAvailable() {
        return !Strings.isNullOrEmpty(balanceType)
                && balanceType.equalsIgnoreCase(FinecoBankConstants.BalanceTypes.INTERIM_AVAILABLE);
    }
}
