package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.initialtransactions.InitialTransactionsFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class TransactionKeyPaginationController<A extends Account, T>
        implements TransactionPaginator<A> {
    private final TransactionKeyPaginator<A, T> paginator;
    // Optional for fetching the first page
    private final InitialTransactionsFetcher<A, TransactionKeyPaginatorResponse<T>>
            initialTransactionFetcher;
    private T nextKey;

    public TransactionKeyPaginationController(TransactionKeyPaginator<A, T> paginator) {
        this(paginator, null);
    }

    public TransactionKeyPaginationController(
            TransactionKeyPaginator<A, T> paginator,
            InitialTransactionsFetcher<A, TransactionKeyPaginatorResponse<T>>
                    initialTransactionFetcher) {
        this.paginator = Preconditions.checkNotNull(paginator);
        this.initialTransactionFetcher = initialTransactionFetcher;
    }

    @Override
    public void resetState() {
        nextKey = null;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        TransactionKeyPaginatorResponse<T> response =
                shouldFetchInitialTransactions()
                        ? initialTransactionFetcher.fetchInitialTransactionsFor(account)
                        : paginator.getTransactionsFor(account, nextKey);

        Preconditions.checkState(
                response.canFetchMore().isPresent(), "canFetchMore must be defined.");
        if (response.canFetchMore().orElse(false)) {
            nextKey = response.nextKey();
        }
        return response;
    }

    private boolean shouldFetchInitialTransactions() {
        return nextKey == null && initialTransactionFetcher != null;
    }
}
