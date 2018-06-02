package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index;

import com.google.common.base.Preconditions;
import java.util.Collection;
import java.util.Collections;
import java.util.Objects;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionIndexPaginationController<A extends Account> implements TransactionPaginator<A> {
    private final TransactionIndexPaginator<A> paginator;
    static int NUMBER_OF_TRANSACTIONS_PER_PAGE = 30;
    int numberOfTransactionsFetched = 0;
    private Account currentAccount;

    public TransactionIndexPaginationController(
            TransactionIndexPaginator<A> paginator) {
        this.paginator = Preconditions.checkNotNull(paginator);
    }

    @Override
    public Collection<? extends Transaction> fetchTransactionsFor(A account) {
        Preconditions.checkState(canFetchMoreFor(account),
                "Fetching more transactions when canFetchMore() returns false is not allowed");
        Collection<? extends Transaction> transactions = paginator.getTransactionsFor(account,
                NUMBER_OF_TRANSACTIONS_PER_PAGE, numberOfTransactionsFetched);

        if (transactions == null || transactions.isEmpty()){
            /*
                In the case of total number of transaction is multiple of number of transaction per page,
                e.g. total of 60 transactions when fetch 30 each time,
                the last transaction will return an empty list.
                therefore number of transactions fetch need to be set to NOT multiple of number of transaction
                per page. in this case, it is set to -1.
             */
            numberOfTransactionsFetched = -1;
            return Collections.emptyList();
        }

        numberOfTransactionsFetched += transactions.size();
        return transactions;
    }

    @Override
    public boolean canFetchMoreFor(A account) {
        resetStateIfAccountChanged(account);
        return numberOfTransactionsFetched % NUMBER_OF_TRANSACTIONS_PER_PAGE == 0;
    }

    private void resetStateIfAccountChanged(Account account) {
        Preconditions.checkNotNull(account);

        if (Objects.equals(currentAccount, account)) {
            return;
        }

        currentAccount = account;
        numberOfTransactionsFetched = 0;
    }
}
