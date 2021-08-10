package se.tink.backend.aggregation.agents.nxgen.at.openbanking.unicredit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;

public class UnicreditATTransactionsDateFromChooserTest {

    private static final ConstantLocalDateTimeSource CONSTANT_LOCAL_DATE_TIME_SOURCE =
            new ConstantLocalDateTimeSource();
    private static final LocalDate NOW = CONSTANT_LOCAL_DATE_TIME_SOURCE.now().toLocalDate();

    private UnicreditATTransactionsDateFromChooser unicreditATTransactionsDateFromChooser =
            new UnicreditATTransactionsDateFromChooser(new ConstantLocalDateTimeSource());

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsPresent() {
        LocalDate dateFrom = unicreditATTransactionsDateFromChooser.selectMinDateFrom(true);

        assertThat(dateFrom).isEqualTo(NOW.minusDays(90));
    }

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsNotPresent() {
        LocalDate dateFrom = unicreditATTransactionsDateFromChooser.selectMinDateFrom(false);

        assertThat(dateFrom).isEqualTo(NOW.withDayOfYear(1).minusYears(1));
    }
}
