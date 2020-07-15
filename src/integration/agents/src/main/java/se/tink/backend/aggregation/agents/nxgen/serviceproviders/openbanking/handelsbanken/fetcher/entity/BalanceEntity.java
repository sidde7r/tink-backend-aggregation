package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class BalanceEntity {

    @JsonProperty("amount")
    private TransactionAmountEntity transactionAmountEntity;

    private String balanceType;

    public TransactionAmountEntity getTransactionAmountEntity() {
        return transactionAmountEntity;
    }

    public String getBalanceType() {
        return balanceType;
    }
}
