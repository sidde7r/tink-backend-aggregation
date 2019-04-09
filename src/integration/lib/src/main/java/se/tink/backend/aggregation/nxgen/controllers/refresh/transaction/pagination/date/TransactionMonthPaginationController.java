package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.base.Preconditions;
import java.time.LocalDate;
import java.time.Month;
import java.time.Year;
import java.time.ZoneId;
import java.util.Collection;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionMonthPaginationController<A extends Account>
        implements TransactionPaginator<A> {
    /**
     * Needs to be set to 1 + 'the minimum number of months check'. On the first day of the month 3
     * months and 1 day will be checked.
     */
    private static final int MAX_CONSECUTIVE_EMPTY_PAGES = 4;

    protected static final int MAX_TOTAL_EMPTY_PAGES = 25;
    private final LocalDate nowInLocalDate;
    private final TransactionMonthPaginator paginator;
    private LocalDate dateToFetch;
    private int consecutiveEmptyFetches = 0;
    private int totalEmptyFetches = 0;
    private boolean foundSomething;

    public TransactionMonthPaginationController(
            TransactionMonthPaginator paginator, ZoneId zoneId) {
        this.paginator = Preconditions.checkNotNull(paginator);
        this.nowInLocalDate = LocalDate.now(zoneId);
    }

    @Override
    public void resetState() {
        consecutiveEmptyFetches = 0;
        totalEmptyFetches = 0;
        foundSomething = false;
        dateToFetch = nowInLocalDate;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        PaginatorResponse response =
                paginator.getTransactionsFor(
                        account, Year.from(dateToFetch), Month.from(dateToFetch));

        Collection<? extends Transaction> transactions = response.getTinkTransactions();

        dateToFetch = dateToFetch.minusMonths(1);

        if (transactions.isEmpty() && !response.canFetchMore().isPresent()) {
            consecutiveEmptyFetches++;
            totalEmptyFetches++;

            return PaginatorResponseImpl.createEmpty(
                    foundSomething
                            ? consecutiveEmptyFetches < MAX_CONSECUTIVE_EMPTY_PAGES
                            : totalEmptyFetches < MAX_TOTAL_EMPTY_PAGES);
        }

        consecutiveEmptyFetches = 0;
        foundSomething = true;

        if (!response.canFetchMore().isPresent()) {
            // If canFetchMore is not defined we assume we always can fetch more (until we reach an
            // empty page).
            return PaginatorResponseImpl.create(transactions, true);
        }

        return response;
    }
}
