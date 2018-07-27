package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import com.google.common.base.Preconditions;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class TransactionKeyPaginationController<A extends Account, T> implements TransactionPaginator<A> {
    private final TransactionKeyPaginator<A, T> paginator;

    private Account currentAccount;
    private T nextKey;

    public TransactionKeyPaginationController(TransactionKeyPaginator<A, T> paginator) {
        this.paginator = Preconditions.checkNotNull(paginator);
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        resetStateIfAccountChanged(account);

        TransactionKeyPaginatorResponse<T> response = paginator.getTransactionsFor(account, nextKey);
        Preconditions.checkState(response.canFetchMore().isPresent(), "canFetchMore must be defined.");

        nextKey = response.nextKey();
        return response;
    }

    private void resetStateIfAccountChanged(Account account) {
        if (!Objects.equals(account, currentAccount)) {
            currentAccount = account;
            nextKey = null;
        }
    }
}
