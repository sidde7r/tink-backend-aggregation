package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class TransactionPagePaginationController<A extends Account> implements TransactionPaginator<A> {
    private final TransactionPagePaginator<A> paginator;
    private A currentAccount;
    private int startPage;
    private int currentPage;

    public TransactionPagePaginationController(TransactionPagePaginator<A> paginator, int startPage) {
        this.paginator = paginator;
        this.startPage = startPage;
        this.currentPage = startPage;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        resetStateIfAccountChanged(account);

        PaginatorResponse response = paginator.getTransactionsFor(account, currentPage);
        Preconditions.checkState(response.canFetchMore().isPresent(), "canFetchMore must be defined.");

        currentPage++;

        return response;
    }

    private void resetStateIfAccountChanged(A account) {
        if (!Objects.equals(currentAccount, account)) {
            currentAccount = account;
            currentPage = startPage;
        }
    }
}
