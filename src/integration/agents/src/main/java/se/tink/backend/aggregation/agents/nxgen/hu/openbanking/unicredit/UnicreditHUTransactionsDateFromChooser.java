package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit;

import java.time.LocalDate;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.unicredit.fetcher.transactionalaccount.UnicreditTransactionsDateFromChooser;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class UnicreditHUTransactionsDateFromChooser extends UnicreditTransactionsDateFromChooser {

    private static final LocalDate NOT_LIMITED_DATE_FROM = LocalDate.ofYearDay(1970, 1);

    public UnicreditHUTransactionsDateFromChooser(LocalDateTimeSource localDateTimeSource) {
        super(localDateTimeSource);
    }

    @Override
    protected LocalDate selectMinDateFrom(boolean firstFetch) {
        return NOT_LIMITED_DATE_FROM;
    }
}
