package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.creditcard.entity;

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
}
