package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import java.time.LocalDate;
import java.time.Period;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.UnicreditTransactionsDateFromChooser;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class HvbOnlineUnicreditTransactionsDateFromChooser
        extends UnicreditTransactionsDateFromChooser {

    private static final Period DAYS_AFTER_CONSENT_INITIALIZATION = Period.ofDays(750);
    private static final Period FOLLOWING_DAYS_AFTER_CONSENT_INITIALIZATION = Period.ofDays(90);

    public HvbOnlineUnicreditTransactionsDateFromChooser(LocalDateTimeSource localDateTimeSource) {
        super(localDateTimeSource);
    }

    @Override
    protected LocalDate selectMinDateFrom(boolean firstFetch) {
        return firstFetch
                ? subtract(DAYS_AFTER_CONSENT_INITIALIZATION)
                : subtract(FOLLOWING_DAYS_AFTER_CONSENT_INITIALIZATION);
    }
}
