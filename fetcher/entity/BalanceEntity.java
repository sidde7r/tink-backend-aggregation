package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.fetcher.entity;

import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabConstants.BalanceTypes;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {
    private AmountEntity balanceAmount;
    private String balanceType;
    private Boolean creditLimitIncluded;

    public AmountEntity getBalanceAmount() {
        return balanceAmount;
    }

    public Boolean isInterimBooked() {
        return BalanceTypes.INTERIM_BOOKED.equalsIgnoreCase(balanceType);
    }
}
