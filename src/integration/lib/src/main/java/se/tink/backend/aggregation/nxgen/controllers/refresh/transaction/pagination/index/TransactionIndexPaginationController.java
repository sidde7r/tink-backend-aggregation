package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.index;

import com.google.common.base.Preconditions;
import java.util.Collection;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionIndexPaginationController<A extends Account>
        implements TransactionPaginator<A> {
    private final TransactionIndexPaginator<A> paginator;
    private static final int NUMBER_OF_TRANSACTIONS_PER_PAGE = 30;
    private int numberOfTransactionsFetched = 0;

    public TransactionIndexPaginationController(TransactionIndexPaginator<A> paginator) {
        this.paginator = Preconditions.checkNotNull(paginator);
    }

    @Override
    public void resetState() {
        numberOfTransactionsFetched = 0;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        PaginatorResponse response =
                paginator.getTransactionsFor(
                        account, NUMBER_OF_TRANSACTIONS_PER_PAGE, numberOfTransactionsFetched);

        Collection<? extends Transaction> transactions = response.getTinkTransactions();
        numberOfTransactionsFetched += transactions.size();

        if (transactions.size() < NUMBER_OF_TRANSACTIONS_PER_PAGE
                && !response.canFetchMore().isPresent()) {
            // If we return less transactions than we asked for AND the pagee doesn't implement
            // canFetchMore we
            // abort (i.e. we've reached the last page). However, we return the transactions we
            // managed to fetch.
            return PaginatorResponseImpl.create(transactions, false);
        }

        if (!response.canFetchMore().isPresent()) {
            // If canFetchMore is not defined we assume we always can fetch more (until we reach a
            // page with less
            // transactions than what we asked for).
            return PaginatorResponseImpl.create(transactions, true);
        }

        return response;
    }
}
