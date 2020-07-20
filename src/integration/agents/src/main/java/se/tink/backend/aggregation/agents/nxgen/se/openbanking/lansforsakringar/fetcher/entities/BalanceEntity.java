package se.tink.backend.aggregation.agents.nxgen.se.openbanking.lansforsakringar.fetcher.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private BalanceAmountEntity balanceAmount;
    private String balanceType;

    public String getBalanceType() {
        return balanceType;
    }

    public BalanceAmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public boolean isAvailableBalance() {
        return balanceType.equalsIgnoreCase("AUTHORIZED");
    }
}
