package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination;

import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@EqualsAndHashCode
public class PaginatorResponseImpl implements PaginatorResponse {

    private static final Collection<? extends Transaction> EMPTY_LIST = Collections.emptyList();
    private final Collection<? extends Transaction> transactions;
    private final Boolean canFetchMore;

    private PaginatorResponseImpl(
            Collection<? extends Transaction> transactions, Boolean canFetchMore) {
        this.transactions = transactions;
        this.canFetchMore = canFetchMore;
    }

    /**
     * Create an empty PaginatorResponse without indicating there are more transactions to fetch.
     * Pagination will continue until canFetchMore is false or max consecutive empty pages limit has
     * been reached.
     */
    public static PaginatorResponse createEmpty() {
        return new PaginatorResponseImpl(EMPTY_LIST, null);
    }

    /**
     * Create an empty PaginatorResponse and explicitly indicate if there are more transactions to
     * fetch.
     */
    public static PaginatorResponse createEmpty(boolean canFetchMore) {
        return new PaginatorResponseImpl(EMPTY_LIST, canFetchMore);
    }

    /**
     * Create an empty PaginatorResponse and explicitly indicate that there are no more transactions
     * to fetch. Pagination will stop after this response has been returned.
     */
    public static PaginatorResponse createEmptyFinal() {
        return new PaginatorResponseImpl(EMPTY_LIST, false);
    }

    public static PaginatorResponse create(Collection<? extends Transaction> transactions) {
        return new PaginatorResponseImpl(transactions, null);
    }

    public static PaginatorResponse create(
            Collection<? extends Transaction> transactions, boolean canFetchMore) {
        return new PaginatorResponseImpl(transactions, canFetchMore);
    }

    @Override
    public Collection<? extends Transaction> getTinkTransactions() {
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.ofNullable(canFetchMore);
    }
}
