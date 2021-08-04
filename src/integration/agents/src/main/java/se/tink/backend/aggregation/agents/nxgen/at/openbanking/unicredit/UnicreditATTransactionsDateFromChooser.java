package se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit;

import java.time.LocalDate;
import java.time.Period;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.UnicreditTransactionsDateFromChooser;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class UnicreditATTransactionsDateFromChooser extends UnicreditTransactionsDateFromChooser {

    private static final Period YEARS_AFTER_CONSENT_INITIALIZATION = Period.ofYears(2);
    private static final Period FOLLOWING_DAYS_AFTER_CONSENT_INITIALIZATION = Period.ofDays(90);

    public UnicreditATTransactionsDateFromChooser(LocalDateTimeSource localDateTimeSource) {
        super(localDateTimeSource);
    }

    @Override
    protected LocalDate selectMinDateFrom(boolean firstFetch) {
        return firstFetch
                ? subtract(FOLLOWING_DAYS_AFTER_CONSENT_INITIALIZATION)
                : subtractYearsCountingCurrentAsOne(YEARS_AFTER_CONSENT_INITIALIZATION);
    }
}
