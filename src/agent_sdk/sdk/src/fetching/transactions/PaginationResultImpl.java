package se.tink.agent.sdk.fetching.transactions;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import lombok.EqualsAndHashCode;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@EqualsAndHashCode
public class PaginationResultImpl implements PaginationResult {

    private static final List<Transaction> EMPTY_LIST = Collections.emptyList();
    private final List<Transaction> transactions;
    private final Boolean canFetchMore;

    private PaginationResultImpl(List<Transaction> transactions, Boolean canFetchMore) {
        this.transactions = transactions;
        this.canFetchMore = canFetchMore;
    }

    public static PaginationResult createEmpty() {
        return new PaginationResultImpl(EMPTY_LIST, null);
    }

    public static PaginationResult createEmpty(boolean canFetchMore) {
        return new PaginationResultImpl(EMPTY_LIST, canFetchMore);
    }

    public static PaginationResult create(List<Transaction> transactions) {
        return new PaginationResultImpl(transactions, null);
    }

    public static PaginationResult create(List<Transaction> transactions, boolean canFetchMore) {
        return new PaginationResultImpl(transactions, canFetchMore);
    }

    @Override
    public List<Transaction> getTinkTransactions() {
        return transactions;
    }

    @Override
    public Optional<Boolean> canFetchMore() {
        return Optional.ofNullable(canFetchMore);
    }
}
