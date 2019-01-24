package se.tink.backend.aggregation.agents;

import java.util.List;
import java.util.Map;
import se.tink.backend.aggregation.rpc.Account;
import se.tink.backend.aggregation.agents.models.Transaction;

public class FetchTransactionsResponse {
    private final Map<Account, List<Transaction>> transactions;

    public FetchTransactionsResponse(
            Map<Account, List<Transaction>> transactions) {
        this.transactions = transactions;
    }

    public Map<Account, List<Transaction>> getTransactions() {
        return transactions;
    }
}
