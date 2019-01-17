package se.tink.backend.aggregation.agents;

import java.util.List;
import java.util.Map;
import se.tink.backend.system.rpc.Transaction;

public class RefreshTransactionsResponse {
    private Map<String, List<Transaction>> transactions;

    public Map<String, List<Transaction>> getTransactions() {
        return transactions;
    }

    public void setTransactions(Map<String, List<Transaction>> transactions) {
        this.transactions = transactions;
    }
}
