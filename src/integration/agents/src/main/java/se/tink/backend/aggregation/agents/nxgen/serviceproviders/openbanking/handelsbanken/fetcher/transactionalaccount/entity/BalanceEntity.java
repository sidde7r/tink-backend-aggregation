package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    private String balanceType;
    private AmountEntity amount;

    public String getBalanceType() {
        return balanceType;
    }

    public AmountEntity getAmount() {
        return amount;
    }
}
