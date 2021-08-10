package se.tink.backend.aggregation.agents.nxgen.it.openbanking.unicredit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class UnicreditITTransactionsDateFromChooserTest {

    private static final LocalDateTimeSource LOCAL_DATE_TIME_SOURCE =
            new ConstantLocalDateTimeSource();

    private UnicreditITTransactionsDateFromChooser unicreditITTransactionsDateFromChooser =
            new UnicreditITTransactionsDateFromChooser(LOCAL_DATE_TIME_SOURCE);

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsPresent() {
        LocalDate dateFrom = unicreditITTransactionsDateFromChooser.selectMinDateFrom(true);

        assertThat(dateFrom)
                .isEqualTo(
                        LOCAL_DATE_TIME_SOURCE.now().withDayOfYear(1).minusYears(9).toLocalDate());
    }

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsNotPresent() {
        LocalDate dateFrom = unicreditITTransactionsDateFromChooser.selectMinDateFrom(false);

        assertThat(dateFrom)
                .isEqualTo(
                        LOCAL_DATE_TIME_SOURCE.now().withDayOfYear(1).minusYears(9).toLocalDate());
    }
}
