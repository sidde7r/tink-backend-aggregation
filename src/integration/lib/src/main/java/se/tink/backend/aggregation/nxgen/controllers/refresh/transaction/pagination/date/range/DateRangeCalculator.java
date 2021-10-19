package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range;

import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@RequiredArgsConstructor
public class DateRangeCalculator {

    private final LocalDateTimeSource timeSource;
    private final ZoneOffset offset;

    public OffsetDateTime calculateTo(OffsetDateTime from) {
        if (from == null) {
            return timeSource.now().atOffset(offset);
        }

        return from.minusDays(1).with(LocalTime.MAX);
    }

    public OffsetDateTime calculateFromAsStartOfTheDayWithLimit(
            OffsetDateTime to, Period period, OffsetDateTime limit) {
        OffsetDateTime from = to.minus(period).with(LocalTime.MIN);

        if (from.isBefore(limit)) {
            return limit;
        }

        return from;
    }
}
