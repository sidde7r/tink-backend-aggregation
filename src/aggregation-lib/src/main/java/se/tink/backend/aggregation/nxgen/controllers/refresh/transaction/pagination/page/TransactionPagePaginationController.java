package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import java.util.Collection;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionPagePaginationController<A extends Account> implements TransactionPaginator<A> {
    private final TransactionPagePaginator<A> paginator;
    private A currentAccount;
    private int startPage;
    private int currentPage;
    private boolean canFetchMore;

    public TransactionPagePaginationController(TransactionPagePaginator<A> paginator, int startPage) {
        this.paginator = paginator;
        this.startPage = startPage;
        this.currentPage = startPage;
    }

    @Override
    public Collection<? extends Transaction> fetchTransactionsFor(A account) {
        resetStateIfAccountChanged(account);

        TransactionPagePaginatorResponse response = paginator.getTransactionsFor(account, currentPage);
        this.canFetchMore = response.canFetchMore();
        currentPage++;

        return response.getTinkTransactions();
    }

    @Override
    public boolean canFetchMoreFor(A account) {
        return canFetchMore;
    }

    private void resetStateIfAccountChanged(A account) {
        if (!Objects.equals(currentAccount, account)) {
            currentAccount = account;
            currentPage = startPage;
        }
    }
}
