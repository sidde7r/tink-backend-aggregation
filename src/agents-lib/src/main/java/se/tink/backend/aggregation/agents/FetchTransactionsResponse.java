package se.tink.backend.aggregation.agents;

import java.util.List;
import java.util.Map;
import se.tink.backend.system.rpc.Transaction;

public class FetchTransactionsResponse {
    private final Map<String, List<Transaction>> transactions;

    public FetchTransactionsResponse(
            Map<String, List<Transaction>> transactions) {
        this.transactions = transactions;
    }

    public Map<String, List<Transaction>> getTransactions() {
        return transactions;
    }
}
