package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.page;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionKeyPaginationController<A extends Account, T> implements TransactionPaginator<A> {
    private final TransactionKeyPaginator<A, T> paginator;

    private Account currentAccount;
    private boolean hasNext = true;
    private T nextKey;

    public TransactionKeyPaginationController(TransactionKeyPaginator<A, T> paginator) {
        this.paginator = Preconditions.checkNotNull(paginator);
    }

    @Override
    public Collection<? extends Transaction> fetchTransactionsFor(A account) {
        Preconditions.checkState(canFetchMoreFor(account),
                String.format("Fetching more transactions when canFetchMore() returns false is not allowed account [%s], current [%s]",
                        account.getAccountNumber(), getCurrentAccountNumber()));

        TransactionKeyPaginatorResponse<T> response = fetchTransactionsFor(account, nextKey);
        hasNext = response.hasNext();
        nextKey = response.nextKey();

        return response.getTinkTransactions() != null ? response.getTinkTransactions() : Collections.emptyList();
    }

    private TransactionKeyPaginatorResponse<T> fetchTransactionsFor(A account, T nextKey) {
        return Optional.ofNullable(paginator.getTransactionsFor(account, nextKey))
                .orElseGet(TransactionKeyPaginatorResponseImpl::new);
    }

    @Override
    public boolean canFetchMoreFor(Account account) {
        resetStateIfAccountChanged(account);

        return hasNext;
    }

    private void resetStateIfAccountChanged(Account account) {
        if (!Objects.equals(account, currentAccount)) {
            currentAccount = account;
            hasNext = true;
            nextKey = null;
        }
    }

    private String getCurrentAccountNumber() {
        if (currentAccount != null) {
            return currentAccount.getAccountNumber();
        }

        return "N/A";
    }
}
