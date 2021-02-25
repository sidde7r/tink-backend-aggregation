package se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.fetcher.card.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import se.tink.backend.aggregation.agents.nxgen.it.openbanking.finecobank.FinecoBankConstants.BalanceTypes;
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

    @JsonIgnore
    public boolean isForwardBalanceAvailable() {
        return BalanceTypes.FORWARD_AVAILABLE.equalsIgnoreCase(balanceType);
    }

    @JsonIgnore
    public boolean isInterimBalanceAvailable() {
        return BalanceTypes.INTERIM_AVAILABLE.equalsIgnoreCase(balanceType);
    }
}
