package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.balance;

import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.SdcConstants;
import se.tink.backend.aggregation.agents.nxgen.se.openbanking.sdc.fetcher.transactionalaccount.entity.common.AmountEntity;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private AmountEntity balanceAmount;
    private String balanceType;
    private String referenceDate;

    public AmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public boolean isAvailable() {
        return balanceType.equalsIgnoreCase(SdcConstants.Account.AVAILABLE);
    }
}
