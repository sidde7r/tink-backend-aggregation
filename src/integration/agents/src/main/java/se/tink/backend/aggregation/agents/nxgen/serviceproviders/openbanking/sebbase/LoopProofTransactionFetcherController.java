package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.sebbase;

import com.google.common.base.Preconditions;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.TransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.UpcomingTransactionFetcher;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.AggregationTransaction;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.backend.aggregation.nxgen.core.transaction.UpcomingTransaction;

/**
 * LoopProofTransactionFetcherController is only workaround and its copied from
 * TransactionFetcherController works exactly the same with one exception, it checks if resource id
 * is already on a results list and breaks the loop.
 *
 * <p>Code should be removed when bank fixes its api.
 *
 * @param <A>
 */
@Deprecated
public class LoopProofTransactionFetcherController<A extends Account>
        implements TransactionFetcher<A> {

    private final TransactionPaginator<A> paginator;
    private final UpcomingTransactionFetcher upcomingTransactionFetcher;
    private final TransactionPaginationHelper paginationHelper;

    public LoopProofTransactionFetcherController(
            TransactionPaginationHelper paginationHelper, TransactionPaginator<A> paginator) {
        this(paginationHelper, paginator, null);
    }

    public LoopProofTransactionFetcherController(
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

        Set<String> ids = new HashSet<>();
        do {
            PaginatorResponse response = paginator.fetchTransactionsFor(account);
            if (response != null) {
                Collection<? extends Transaction> tinkTransactions = response.getTinkTransactions();
                if (tinkTransactions != null) {

                    Set<String> pageTransactionIds = extractIdsFromCurrentPage(tinkTransactions);
                    if (ids.containsAll(pageTransactionIds)) {
                        transactions.addAll(response.getTinkTransactions());
                        ids.addAll(pageTransactionIds);
                    } else {
                        break;
                    }
                }
                if (!response.canFetchMore()
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "Pagee must indicate canFetchMore!"))) {
                    break;
                }
            } else {
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

    private Set<String> extractIdsFromCurrentPage(
            Collection<? extends Transaction> tinkTransactions) {

        Set<String> resultIds =
                tinkTransactions.stream()
                        .map(t -> ((Transaction) t).getExternalId())
                        .collect(Collectors.toSet());

        return resultIds;
    }
}
