package se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.featcher.transactional;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.Optional;
import se.tink.backend.aggregation.agents.nxgen.uk.openbanking.starling.StarlingConstants;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponse;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.PaginatorResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginator;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginationController;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.TransactionDatePaginator;
import se.tink.backend.aggregation.nxgen.core.account.Account;

public class StarlingTransactionPaginationController<A extends Account>
        implements TransactionPaginator<A> {
    private static final int TIME_FRAME_IN_DAYS = 89;
    private final TransactionDatePaginationController<A> defaultPaginationController;
    private final TransactionDatePaginator<A> paginator;
    private final LocalDateTimeSource localDateTimeSource;
    private final ZoneId defaultZoneId;
    private LocalDateTime fromDateTime;
    private LocalDateTime toDateTime;

    public StarlingTransactionPaginationController(
            TransactionDatePaginationController<A> defaultPaginationController,
            TransactionDatePaginator<A> paginator,
            LocalDateTimeSource localDateTimeSource,
            ZoneId defaultZoneId) {
        this.defaultPaginationController = defaultPaginationController;
        this.paginator = paginator;
        this.localDateTimeSource = localDateTimeSource;
        this.defaultZoneId = defaultZoneId;
    }

    @Override
    public void resetState() {
        fromDateTime = null;
        toDateTime = null;
        defaultPaginationController.resetState();
    }

    @Override
    public PaginatorResponse fetchTransactionsFor(A account) {
        Optional<LocalDateTime> accountCreationDateTime =
                account.getFromTemporaryStorage(
                        StarlingConstants.ACCOUNT_CREATION_DATE_TIME, LocalDateTime.class);

        if (accountCreationDateTime.isPresent()) {
            LocalDateTime creationDate = accountCreationDateTime.get();
            toDateTime = calculateToDate();
            fromDateTime =
                    calculateFromDateAsBeginningOfTheDayBasedOnUnit(toDateTime, creationDate);
            PaginatorResponse transactions =
                    paginator.getTransactionsFor(
                            account, convertToDate(fromDateTime), convertToDate(toDateTime));
            return PaginatorResponseImpl.create(
                    transactions.getTinkTransactions(), !fromDateTime.isEqual(creationDate));
        } else {
            return defaultPaginationController.fetchTransactionsFor(account);
        }
    }

    private Date convertToDate(LocalDateTime dateTime) {
        return Date.from(dateTime.atZone(defaultZoneId).toInstant());
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

    private LocalDateTime calculateFromDateAsBeginningOfTheDayBasedOnUnit(
            LocalDateTime toDate, LocalDateTime creationDate) {
        LocalDateTime calculatedDate = toDate.minusDays(TIME_FRAME_IN_DAYS).with(LocalTime.MIN);
        return creationDate.isAfter(calculatedDate) ? creationDate : calculatedDate;
    }
}
