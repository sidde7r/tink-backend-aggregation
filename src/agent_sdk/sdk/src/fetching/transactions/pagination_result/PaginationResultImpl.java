package se.tink.agent.sdk.fetching.transactions.pagination_result;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@EqualsAndHashCode
public class PaginationResultImpl implements PaginationResult {

    private static final List<? extends Transaction> EMPTY_LIST = Collections.emptyList();
    private final List<? extends Transaction> transactions;
    private final Boolean canFetchMore;

    private PaginationResultImpl(List<? extends Transaction> transactions, Boolean canFetchMore) {
        this.transactions = transactions;
        this.canFetchMore = canFetchMore;
    }

    /**
     * Create an empty PaginationResult without indicating there are more transactions to fetch.
     * Pagination will continue until canFetchMore is false or max consecutive empty pages limit has
     * been reached.
     */
    public static PaginationResult createEmpty() {
        return new PaginationResultImpl(EMPTY_LIST, null);
    }

    /**
     * Create an empty PaginationResult and explicitly indicate if there are more transactions to
     * fetch.
     */
    public static PaginationResult createEmpty(boolean canFetchMore) {
        return new PaginationResultImpl(EMPTY_LIST, canFetchMore);
    }

    /**
     * Create an empty PaginationResult and explicitly indicate that there are no more transactions
     * to fetch. Pagination will stop after this response has been returned.
     */
    public static PaginationResult createEmptyFinal() {
        return new PaginationResultImpl(EMPTY_LIST, false);
    }

    public static PaginationResult create(List<? extends Transaction> transactions) {
        return new PaginationResultImpl(transactions, null);
    }

    public static PaginationResult create(
            List<? extends Transaction> transactions, boolean canFetchMore) {
        return new PaginationResultImpl(transactions, canFetchMore);
    }

    @Override
    public List<? extends Transaction> getTinkTransactions() {
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.ofNullable(canFetchMore);
    }
}
