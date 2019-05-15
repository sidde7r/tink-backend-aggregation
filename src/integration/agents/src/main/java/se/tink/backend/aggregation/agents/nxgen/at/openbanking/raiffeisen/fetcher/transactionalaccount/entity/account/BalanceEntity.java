package se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.fetcher.transactionalaccount.entity.account;

import com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.nxgen.at.openbanking.raiffeisen.RaiffeisenConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private AmountEntity balanceAmount;
    private String balanceType;
    private String lastChangeDateTime;
    private String referenceDate;
    private String lastCommittedTransaction;

    public AmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public boolean isForwardBalanceAvailable() {
        return !Strings.isNullOrEmpty(balanceType)
                && balanceType.equalsIgnoreCase(RaiffeisenConstants.BalanceTypes.FORWARD_AVAILABLE);
    }

    public boolean isInterimBalanceAvailable() {
        return !Strings.isNullOrEmpty(balanceType)
                && balanceType.equalsIgnoreCase(RaiffeisenConstants.BalanceTypes.INTERIM_AVAILABLE);
    }
}
