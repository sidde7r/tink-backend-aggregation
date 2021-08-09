package se.tink.backend.aggregation.agents.nxgen.it.openbanking.buddybank;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;

public class BuddyBankUnicreditTransactionsDateFromChooserTest {

    private static final ConstantLocalDateTimeSource CONSTANT_LOCAL_DATE_TIME_SOURCE =
            new ConstantLocalDateTimeSource();
    private static final LocalDate NOW = CONSTANT_LOCAL_DATE_TIME_SOURCE.now().toLocalDate();

    private BuddyBankUnicreditTransactionsDateFromChooser dateFromChooser =
            new BuddyBankUnicreditTransactionsDateFromChooser(CONSTANT_LOCAL_DATE_TIME_SOURCE);

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsPresent() {
        LocalDate dateFrom = dateFromChooser.selectMinDateFrom(false);

        assertThat(dateFrom).isEqualTo(NOW.withDayOfYear(1).minusYears(9));
    }

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsNotPresent() {
        LocalDate dateFrom = dateFromChooser.selectMinDateFrom(true);

        assertThat(dateFrom).isEqualTo(NOW.withDayOfYear(1).minusYears(9));
    }
}
