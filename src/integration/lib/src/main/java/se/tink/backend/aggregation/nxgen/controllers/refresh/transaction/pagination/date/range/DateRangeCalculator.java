package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;

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
        return helper.getTransactionDateLimit(account)
                .map(certainDate -> new java.sql.Date(certainDate.getTime()).toLocalDate())
                .map(certainLocalDate -> certainLocalDate.atStartOfDay().atOffset(offset))
                .orElse(proposed);
    }
}
