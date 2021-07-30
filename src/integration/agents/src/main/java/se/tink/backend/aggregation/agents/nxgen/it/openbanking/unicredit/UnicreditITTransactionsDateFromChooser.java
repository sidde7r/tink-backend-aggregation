package se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit;

import java.time.LocalDate;
import java.time.Period;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.UnicreditTransactionsDateFromChooser;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class UnicreditITTransactionsDateFromChooser extends UnicreditTransactionsDateFromChooser {

    private static final Period YEARS_AFTER_CONSENT_INITIALIZATION = Period.ofYears(10);
    private static final Period FOLLOWING_YEARS_AFTER_CONSENT_INITIALIZATION = Period.ofYears(10);

    public UnicreditITTransactionsDateFromChooser(LocalDateTimeSource localDateTimeSource) {
        super(localDateTimeSource);
    }

    @Override
    protected LocalDate selectMinDateFrom(boolean firstFetch) {
        return firstFetch
                ? subtractYearsCountingCurrentAsOne(FOLLOWING_YEARS_AFTER_CONSENT_INITIALIZATION)
                : subtractYearsCountingCurrentAsOne(YEARS_AFTER_CONSENT_INITIALIZATION);
    }
}
