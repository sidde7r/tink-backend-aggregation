package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class Balance {

    @JsonProperty("amount")
    private TransactionAmount transactionAmount;

    @JsonProperty("balanceType")
    private String balanceType;

    public TransactionAmount getTransactionAmount() {
        return transactionAmount;
    }

    public String getBalanceType() {
        return balanceType;
    }
}
