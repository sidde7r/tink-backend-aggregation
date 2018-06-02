package se.tink.backend.rpc;

import io.protostuff.Tag;
import java.util.List;

import se.tink.backend.core.Transaction;
import se.tink.backend.core.TransactionQuery;

public class TransactionQueryResponse {
    @Tag(1)
    protected int count;
    @Tag(2)
    protected TransactionQuery query;
    @Tag(3)
    protected List<Transaction> transactions;

    public int getCount() {
        return count;
    }

    public TransactionQuery getQuery() {
        return query;
    }

    public List<Transaction> getTransactions() {
        return transactions;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setQuery(TransactionQuery query) {
        this.query = query;
    }

    public void setTransactions(List<Transaction> transactions) {
        this.transactions = transactions;
    }
}
