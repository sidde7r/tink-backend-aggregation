package se.tink.backend.aggregation.agents.creditcards.americanexpress.v3.model;

public class TransactionsResponse {
    private TransactionDetailsEntity transactionDetails;

    public TransactionDetailsEntity getTransactionDetails() {
        return transactionDetails;
    }

    public void setTransactionDetails(TransactionDetailsEntity transactionDetails) {
        this.transactionDetails = transactionDetails;
    }
}
