package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.base.Preconditions;
import java.lang.invoke.MethodHandles;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

public class TransactionDatePaginationController<A extends Account>
        implements TransactionPaginator<A> {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private static final int DEFAULT_MAX_CONSECUTIVE_EMPTY_PAGES = 4;
    private static final int DEFAULT_DAYS_TO_FETCH = 89;

    private final TransactionDatePaginator<A> paginator;

    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;
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
        this(paginator, consecutiveEmptyPagesLimit, DEFAULT_DAYS_TO_FETCH, ChronoUnit.DAYS);
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
            LocalDateTimeSource localDateTimeSource) {
        this(
                paginator,
                consecutiveEmptyPagesLimit,
                DEFAULT_DAYS_TO_FETCH,
                ChronoUnit.DAYS,
                localDateTimeSource);
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
        fromDateTime = null;
        toDateTime = null;
        consecutiveEmptyPages = 0;
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        toDateTime = calculateToDate();
        fromDateTime = calculateFromDateAsBeginningOfTheDayBasedOnUnit(toDateTime);

        PaginatorResponse response =
                paginator.getTransactionsFor(
                        account, convertToDate(fromDateTime), convertToDate(toDateTime));

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

    private Date convertToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(ZoneId.of("CET")).toInstant());
    }

    private LocalDateTime calculateToDate() {
        if (toDateTime == null) {
            return localDateTimeSource.now();
        }
        return getEndOfTheDayBeforeLastFromDate();
    }

    private LocalDateTime getEndOfTheDayBeforeLastFromDate() {
        return fromDateTime.minusDays(1).with(LocalTime.MAX);
    }

    private LocalDateTime calculateFromDateAsBeginningOfTheDayBasedOnUnit(LocalDateTime toDate) {
        switch (unitToFetch) {
            case DAYS:
                return toDate.minusDays(amountToFetch).with(LocalTime.MIN);
            case WEEKS:
                return toDate.minusDays(7L * amountToFetch).with(LocalTime.MIN);
            case MONTHS:
                return toDate.minusMonths(amountToFetch).with(LocalTime.MIN);
            default:
                throw new IllegalStateException(
                        "Unsupported pagination unit: " + unitToFetch.toString());
        }
    }
}
