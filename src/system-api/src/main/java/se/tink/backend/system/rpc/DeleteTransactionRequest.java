package se.tink.backend.system.rpc;

import java.util.List;

public class DeleteTransactionRequest {
    private List<TransactionToDelete> transactions;
    private String userId;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<TransactionToDelete> getTransactions() {
        return transactions;
    }

    public void setTransactions(List<TransactionToDelete> transactions) {
        this.transactions = transactions;
    }
}
