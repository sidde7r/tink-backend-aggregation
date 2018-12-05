package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class TransactionPagePaginationController<A extends Account> implements TransactionPaginator<A> {
    private final TransactionPagePaginator<A> paginator;
    private final int startPage;
    private int currentPage;

    public TransactionPagePaginationController(TransactionPagePaginator<A> paginator, int startPage) {
        this.paginator = paginator;
        this.startPage = startPage;
        this.currentPage = startPage;
    }

    @Override
    public void resetState() {
        currentPage = startPage;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        PaginatorResponse response = paginator.getTransactionsFor(account, currentPage);
        Preconditions.checkState(response.canFetchMore().isPresent(), "canFetchMore must be defined.");

        currentPage++;

        return response;
    }
}
