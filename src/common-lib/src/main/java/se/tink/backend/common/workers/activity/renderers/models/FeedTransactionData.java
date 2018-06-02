package se.tink.backend.common.workers.activity.renderers.models;

import se.tink.backend.core.Transaction;

public class FeedTransactionData extends ActivityHeader {

    private Transaction transaction;

    public Transaction getTransaction() {
        return transaction;
    }

    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }
}
