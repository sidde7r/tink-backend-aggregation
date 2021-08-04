package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount;

import java.time.LocalDate;
import java.time.Period;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@RequiredArgsConstructor
public abstract class UnicreditTransactionsDateFromChooser {

    private static final ZoneId ZONE_ID = ZoneId.from(ZoneOffset.UTC);

    private final LocalDateTimeSource localDateTimeSource;

    public LocalDate getDateFrom(Optional<LocalDate> lastTransactionsDateFetched) {
        LocalDate minDateFrom = selectMinDateFrom(lastTransactionsDateFetched.isPresent());
        if (lastTransactionsDateFetched.isPresent()) {
            return lastTransactionsDateFetched.get().isAfter(minDateFrom)
                    ? lastTransactionsDateFetched.get()
                    : minDateFrom;
        }
        return minDateFrom;
    }

    protected abstract LocalDate selectMinDateFrom(boolean firstFetch);

    protected LocalDate subtractYearsCountingCurrentAsOne(Period yearsToSubtract) {
        return now().withDayOfYear(1).minus(yearsToSubtract.minusYears(1L));
    }

    protected LocalDate subtract(Period daysToSubtract) {
        return now().minus(daysToSubtract);
    }

    protected LocalDate now() {
        return localDateTimeSource.getInstant().atZone(ZONE_ID).toLocalDate();
    }
}
