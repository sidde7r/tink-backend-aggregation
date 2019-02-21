package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class TransactionKeyPaginationController<A extends Account, T>
        implements TransactionPaginator<A> {
    private final TransactionKeyPaginator<A, T> paginator;
    private T nextKey;

    public TransactionKeyPaginationController(TransactionKeyPaginator<A, T> paginator) {
        this.paginator = Preconditions.checkNotNull(paginator);
    }

    @Override
    public void resetState() {
        nextKey = null;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        TransactionKeyPaginatorResponse<T> response =
                paginator.getTransactionsFor(account, nextKey);
        Preconditions.checkState(
                response.canFetchMore().isPresent(), "canFetchMore must be defined.");
        if (response.canFetchMore().orElseGet(() -> false)) {
            nextKey = response.nextKey();
        }
        return response;
    }
}
