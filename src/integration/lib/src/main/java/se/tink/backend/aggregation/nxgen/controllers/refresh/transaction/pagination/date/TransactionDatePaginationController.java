package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.base.Preconditions;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;
import se.tink.libraries.date.DateUtils;

public class TransactionDatePaginationController<A extends Account>
        implements TransactionPaginator<A> {
    private static final AggregationLogger logger =
            new AggregationLogger(TransactionDatePaginationController.class);
    private static final int DEFAULT_MAX_CONSECUTIVE_EMPTY_PAGES = 4;
    private static final int DEFAULT_MONTHS_TO_FETCH = 3;

    private final TransactionDatePaginator<A> paginator;

    private Date fromDate;
    private Date toDate;
    private int consecutiveEmptyPages = 0;
    private final int consecutiveEmptyPagesLimit;
    private final ChronoUnit unitToFetch;
    private final int amountToFetch;
    private final LocalDateTimeSource localDateTimeSource;

    public TransactionDatePaginationController(TransactionDatePaginator<A> paginator) {
        this(paginator, DEFAULT_MAX_CONSECUTIVE_EMPTY_PAGES);
    }

    public TransactionDatePaginationController(
            TransactionDatePaginator<A> paginator, int consecutiveEmptyPagesLimit) {
        this(paginator, consecutiveEmptyPagesLimit, DEFAULT_MONTHS_TO_FETCH, ChronoUnit.MONTHS);
    }

    public TransactionDatePaginationController(
            TransactionDatePaginator<A> paginator,
            int consecutiveEmptyPagesLimit,
            int amountToFetch,
            ChronoUnit unitToFetch) {
        this(
                paginator,
                consecutiveEmptyPagesLimit,
                amountToFetch,
                unitToFetch,
                new ActualLocalDateTimeSource());
    }

    public TransactionDatePaginationController(
            TransactionDatePaginator<A> paginator,
            int consecutiveEmptyPagesLimit,
            int amountToFetch,
            ChronoUnit unitToFetch,
            LocalDateTimeSource localDateTimeSource) {
        this.paginator = Preconditions.checkNotNull(paginator);
        this.consecutiveEmptyPagesLimit = consecutiveEmptyPagesLimit;
        this.unitToFetch = unitToFetch;
        this.amountToFetch = amountToFetch;
        this.localDateTimeSource = localDateTimeSource;
        Preconditions.checkState(amountToFetch >= 1, "Amount to fetch must be 1 or more.");
        Preconditions.checkState(
                unitToFetch == ChronoUnit.DAYS
                        || unitToFetch == ChronoUnit.WEEKS
                        || unitToFetch == ChronoUnit.MONTHS,
                "Invalid time unit for pagination: " + unitToFetch.toString());
    }

    @Override
    public void resetState() {
        fromDate = null;
        toDate = null;
        consecutiveEmptyPages = 0;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        toDate = calculateToDate();
        fromDate = calculateFromDate(toDate);

        PaginatorResponse response = paginator.getTransactionsFor(account, fromDate, toDate);

        Collection<? extends Transaction> transactions = response.getTinkTransactions();
        if (transactions.isEmpty() && !response.canFetchMore().isPresent()) {
            // Override canFetchMore with consecutive check.

            logger.info(
                    String.format(
                            "Couldn't find any transactions for account with accountNumber: %s",
                            account.getAccountNumber()));

            consecutiveEmptyPages++;
            return PaginatorResponseImpl.createEmpty(
                    consecutiveEmptyPages < consecutiveEmptyPagesLimit);
        }

        logger.info(
                String.format(
                        "Fetched %s transactions for account with accountNumber: %s",
                        transactions.size(), account.getAccountNumber()));

        consecutiveEmptyPages = 0;

        if (!response.canFetchMore().isPresent()) {
            // If canFetchMore is not defined we assume we always can fetch more (until we reach an
            // empty page).
            return PaginatorResponseImpl.create(transactions, true);
        }

        return response;
    }

    private Date calculateToDate() {
        if (toDate == null) {
            return Date.from(localDateTimeSource.getInstant());
        }

        return DateUtils.addDays(fromDate, -1); // Day before the previous fromDate
    }

    private Date calculateFromDate(Date toDate) {
        switch (unitToFetch) {
            case DAYS:
                return DateUtils.addDays(toDate, -amountToFetch);
            case WEEKS:
                return DateUtils.addDays(toDate, -7 * amountToFetch);
            case MONTHS:
                return DateUtils.addMonths(toDate, -amountToFetch);
            default:
                throw new IllegalStateException(
                        "Unsupported pagination unit: " + unitToFetch.toString());
        }
    }
}
