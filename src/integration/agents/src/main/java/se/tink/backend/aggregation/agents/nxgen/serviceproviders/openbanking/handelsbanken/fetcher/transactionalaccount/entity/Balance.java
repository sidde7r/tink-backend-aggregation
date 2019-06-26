package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.handelsbanken.fetcher.transactionalaccount.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import javax.annotation.Generated;

@Generated("com.robohorse.robopojogenerator")
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
