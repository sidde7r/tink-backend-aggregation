package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.HandelsbankenBaseConstants;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalancesItemEntity {

    @JsonProperty("amount")
    private AmountEntity amountEntity;

    private String balanceType;

    public AmountEntity getAmountEntity() {
        return amountEntity;
    }

    public String getBalanceType() {
        return balanceType;
    }

    public boolean isAvailable() {
        return getBalanceType()
                .equalsIgnoreCase(HandelsbankenBaseConstants.AccountBalance.AVAILABLE_BALANCE);
    }

    public boolean isCurrent() {
        return getBalanceType()
                .equalsIgnoreCase(HandelsbankenBaseConstants.AccountBalance.CURRENT_BALANCE);
    }
}
