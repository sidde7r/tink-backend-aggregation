package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionKeyPaginatorResponseImpl<T> implements TransactionKeyPaginatorResponse<T> {
    private Collection<? extends Transaction> transactions;
    private T next;

    public TransactionKeyPaginatorResponseImpl() {
    }

    public TransactionKeyPaginatorResponseImpl(
            Collection<? extends Transaction> transactions, T next) {
        this.transactions = transactions;
        this.next = next;
    }

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
    public Optional<Boolean> canFetchMore() {
        return Optional.of(next != null);
    }

    public static <T> TransactionKeyPaginatorResponseImpl<T> createEmpty() {
        return new TransactionKeyPaginatorResponseImpl<>(Collections.emptyList(), null);
    }
}
