package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PendingTransactionsResponse extends AbstractResponse  {
    private List<TransactionListResponse> transactionGroups;

    public List<TransactionListResponse> getTransactionGroups() {
        return transactionGroups;
    }

    public void setTransactionGroups(List<TransactionListResponse> transactionGroups) {
        this.transactionGroups = transactionGroups;
    }
}
