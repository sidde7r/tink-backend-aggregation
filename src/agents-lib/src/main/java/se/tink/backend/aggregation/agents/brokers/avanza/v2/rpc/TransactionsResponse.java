package se.tink.backend.aggregation.agents.brokers.avanza.v2.rpc;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;
import se.tink.backend.aggregation.agents.brokers.avanza.v2.model.TransactionEntity;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransactionsResponse {
    private List<TransactionEntity> transactions;
    private int totalNumberOfTransactions;

    public List<TransactionEntity> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionEntity> transactions) {
        this.transactions = transactions;
    }

    public int getTotalNumberOfTransactions() {
        return totalNumberOfTransactions;
    }

    public void setTotalNumberOfTransactions(int totalNumberOfTransactions) {
        this.totalNumberOfTransactions = totalNumberOfTransactions;
    }
}
