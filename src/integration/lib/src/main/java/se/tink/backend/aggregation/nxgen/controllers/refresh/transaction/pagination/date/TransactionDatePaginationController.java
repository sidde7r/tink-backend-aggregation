package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date;

import com.google.common.base.Preconditions;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Collection;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ActualLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;
import se.tink.backend.aggregation.nxgen.core.transaction.Transaction;

@Slf4j
public class TransactionDatePaginationController<A extends Account>
        implements TransactionPaginator<A> {

    private static final int DEFAULT_MAX_CONSECUTIVE_EMPTY_PAGES = 4;
    private static final int DEFAULT_DAYS_TO_FETCH = 89;

    private final TransactionDatePaginator<A> paginator;
    private final int consecutiveEmptyPagesLimit;
    private final ChronoUnit unitToFetch;
    private final int amountToFetch;
    private final LocalDateTimeSource localDateTimeSource;
    private final ZoneId zoneId;

    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;
    private int consecutiveEmptyPages = 0;

    protected TransactionDatePaginationController(Builder<A> builder) {
        this.paginator = builder.paginator;
        this.consecutiveEmptyPagesLimit = builder.consecutiveEmptyPagesLimit;
        this.unitToFetch = builder.unitToFetch;
        this.amountToFetch = builder.amountToFetch;
        this.localDateTimeSource = builder.localDateTimeSource;
        this.zoneId = builder.zoneId;
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

            log.info(
                    String.format(
                            "Couldn't find any transactions for account with accountNumber: %s",
                            account.getAccountNumber()));

            consecutiveEmptyPages++;
            return PaginatorResponseImpl.createEmpty(
                    consecutiveEmptyPages < consecutiveEmptyPagesLimit);
        }

        log.info(
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
        return Date.from(dateTime.atZone(zoneId).toInstant());
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

    public static class Builder<A extends Account> {
        private final TransactionDatePaginator<A> paginator;
        private int consecutiveEmptyPagesLimit = DEFAULT_MAX_CONSECUTIVE_EMPTY_PAGES;
        private ChronoUnit unitToFetch = ChronoUnit.DAYS;
        private int amountToFetch = DEFAULT_DAYS_TO_FETCH;
        private LocalDateTimeSource localDateTimeSource = new ActualLocalDateTimeSource();
        private ZoneId zoneId = ZoneId.of("CET");

        public Builder(TransactionDatePaginator<A> paginator) {
            Preconditions.checkNotNull(paginator);
            this.paginator = paginator;
        }

        public Builder<A> setConsecutiveEmptyPagesLimit(int consecutiveEmptyPagesLimit) {
            Preconditions.checkState(
                    consecutiveEmptyPagesLimit >= 0, "Amount to fetch must be positive");
            this.consecutiveEmptyPagesLimit = consecutiveEmptyPagesLimit;
            return this;
        }

        public Builder<A> setUnitToFetch(ChronoUnit unitToFetch) {
            Preconditions.checkNotNull(unitToFetch);
            Preconditions.checkState(
                    unitToFetch == ChronoUnit.DAYS
                            || unitToFetch == ChronoUnit.WEEKS
                            || unitToFetch == ChronoUnit.MONTHS,
                    "Invalid time unit for pagination: {}",
                    unitToFetch.toString());
            this.unitToFetch = unitToFetch;
            return this;
        }

        public Builder<A> setAmountToFetch(int amountToFetch) {
            Preconditions.checkState(amountToFetch > 0, "Amount to fetch must be greater than 0.");
            this.amountToFetch = amountToFetch;
            return this;
        }

        public Builder<A> setLocalDateTimeSource(LocalDateTimeSource localDateTimeSource) {
            this.localDateTimeSource = localDateTimeSource;
            return this;
        }

        public Builder<A> setZoneId(ZoneId zoneId) {
            this.zoneId = zoneId;
            return this;
        }

        public TransactionDatePaginationController<A> build() {
            return new TransactionDatePaginationController<>(this);
        }
    }
}
