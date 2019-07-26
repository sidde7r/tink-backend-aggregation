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

    public boolean isBalance() {
        return getBalanceType()
                .equalsIgnoreCase(HandelsbankenBaseConstants.AccountBalance.AVAILABLE_BALANCE);
    }
}
