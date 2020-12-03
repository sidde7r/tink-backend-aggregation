package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

public class TransactionFetcherController<A extends Account> implements TransactionFetcher<A> {
    private final TransactionPaginator<A> paginator;
    private final UpcomingTransactionFetcher upcomingTransactionFetcher;
    private final TransactionPaginationHelper paginationHelper;

    public TransactionFetcherController(
            TransactionPaginationHelper paginationHelper, TransactionPaginator<A> paginator) {
        this(paginationHelper, paginator, null);
    }

    public TransactionFetcherController(
            TransactionPaginationHelper paginationHelper,
            TransactionPaginator<A> paginator,
            UpcomingTransactionFetcher upcomingTransactionFetcher) {
        this.paginationHelper = Preconditions.checkNotNull(paginationHelper);
        this.paginator = Preconditions.checkNotNull(paginator);
        this.upcomingTransactionFetcher = upcomingTransactionFetcher; // Optional
    }

    @Override
    public List<AggregationTransaction> fetchTransactionsFor(A account) {
        Preconditions.checkNotNull(account);

        // Reset the state, if any, for every new account.
        paginator.resetState();

        List<AggregationTransaction> transactions = Lists.newArrayList();

        transactions.addAll(fetchUpcomingTransactionsFor(account));
        do {
            PaginatorResponse response = paginator.fetchTransactionsFor(account);
            if (response == null) {
                continue;
            }

            Collection<? extends Transaction> transactionsToAdd = response.getTinkTransactions();
            if (transactionsToAdd != null) {
                transactions.addAll(transactionsToAdd);
            }

            if (!response.canFetchMore()
                    .orElseThrow(
                            () -> new IllegalStateException("Pagee must indicate canFetchMore!"))) {
                break;
            }
        } while (!paginationHelper.isContentWithRefresh(account, transactions));

        return transactions;
    }

    private Collection<UpcomingTransaction> fetchUpcomingTransactionsFor(Account account) {
        return Optional.ofNullable(upcomingTransactionFetcher)
                .flatMap(
                        upcomingTransactionFetcher ->
                                Optional.of(account)
                                        .map(
                                                t ->
                                                        upcomingTransactionFetcher
                                                                .fetchUpcomingTransactionsFor(t)))
                .orElseGet(Collections::emptyList);
    }
}
