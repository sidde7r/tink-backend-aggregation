package se.tink.backend.aggregation.agents;

import java.util.List;
import java.util.Map;
import se.tink.backend.system.rpc.Transaction;

public class RefreshTransactionsResponse {
    private Map<String, List<Transaction>> refreshedTransactions;

    public Map<String, List<Transaction>> getRefreshedTransactions() {
        return refreshedTransactions;
    }

    public void setRefreshedTransactions(Map<String, List<Transaction>> refreshedTransactions) {
        this.refreshedTransactions = refreshedTransactions;
    }
}
