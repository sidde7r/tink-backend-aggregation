package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItemEntity {

    private AmountEntity amountEntity;

    private String balanceType;

    public AmountEntity getAmountEntity() {
        return amountEntity;
    }

    public String getBalanceType() {
        return balanceType;
    }
}
