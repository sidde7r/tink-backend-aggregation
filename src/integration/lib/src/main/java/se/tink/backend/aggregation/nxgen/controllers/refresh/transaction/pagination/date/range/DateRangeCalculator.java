package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@Slf4j
@RequiredArgsConstructor
public class DateRangeCalculator<ACCOUNT extends Account> {

    private final LocalDateTimeSource timeSource;
    private final ZoneOffset offset;
    private final TransactionPaginationHelper helper;

    public OffsetDateTime calculateTo(OffsetDateTime from) {
        if (from == null) {
            return timeSource.now().atOffset(offset);
        }

        return from.minusDays(1).with(LocalTime.MAX);
    }

    public OffsetDateTime calculateFromAsStartOfTheDay(OffsetDateTime to, Period period) {
        return to.minus(period).with(LocalTime.MIN);
    }

    public OffsetDateTime calculateFromAsStartOfTheDayWithLimit(
            OffsetDateTime to, Period period, OffsetDateTime limit) {
        OffsetDateTime from = calculateFromAsStartOfTheDay(to, period);

        if (from.isBefore(limit)) {
            return limit;
        }

        return from;
    }

    public OffsetDateTime applyCertainDateLimit(ACCOUNT account, OffsetDateTime proposed) {
        Optional<OffsetDateTime> certainOffsetDateTime =
                helper.getTransactionDateLimit(account)
                        .map(dateToLocalDate())
                        .map(localDateToOffsetDateTime());
        log.info("[TRANSACTION FETCHING] Optional certain date: {}", certainOffsetDateTime);

        if (!certainOffsetDateTime.isPresent()) {
            log.info("[TRANSACTION FETCHING] Missing certain date. No limit applied.");
            return proposed;
        }

        if (certainOffsetDateTime.get().isAfter(proposed)) {
            log.info(
                    "[TRANSACTION FETCHING] Certain date is after proposed date. Applying certain date as limit.");
            return certainOffsetDateTime.get();
        }

        log.info("[TRANSACTION FETCHING] Certain date is before proposed date. No limit applied.");
        return proposed;
    }

    private Function<LocalDate, OffsetDateTime> localDateToOffsetDateTime() {
        return certainLocalDate -> certainLocalDate.atStartOfDay().atOffset(offset);
    }

    private Function<Date, LocalDate> dateToLocalDate() {
        return certainDate -> new java.sql.Date(certainDate.getTime()).toLocalDate();
    }
}
