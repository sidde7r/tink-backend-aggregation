package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import java.util.Collection;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionKeyPaginatorResponseImpl<T> implements TransactionKeyPaginatorResponse<T> {
    private Collection<? extends Transaction> transactions;
    private T next;

    public void setTransactions(Collection<? extends Transaction> transactions) {
        this.transactions = transactions;
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions;
    }

    public void setNext(T next) {
        this.next = next;
    }

    @Override
    public T nextKey() {
        return next;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }
}
