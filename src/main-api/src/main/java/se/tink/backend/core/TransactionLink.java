package se.tink.backend.core;

public class TransactionLink {

    private final Transaction transaction;
    private final Transaction counterpartTransaction;

    public TransactionLink(Transaction transaction, Transaction counterpartTransaction) {
        this.transaction = transaction;
        this.counterpartTransaction = counterpartTransaction;
    }

    public Transaction getTransaction() {
        return transaction;
    }

    public Transaction getCounterpartTransaction() {
        return counterpartTransaction;
    }
}
