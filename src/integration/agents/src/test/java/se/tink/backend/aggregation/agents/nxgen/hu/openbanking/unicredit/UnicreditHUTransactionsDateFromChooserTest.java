package se.tink.backend.aggregation.agents.nxgen.hu.openbanking.unicredit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;

public class UnicreditHUTransactionsDateFromChooserTest {

    private static final LocalDate OLD_DATE = LocalDate.of(1980, 1, 1);

    private UnicreditHUTransactionsDateFromChooser unicreditHUTransactionsDateFromChooser =
            new UnicreditHUTransactionsDateFromChooser(new ConstantLocalDateTimeSource());

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsPresent() {
        LocalDate dateFrom = unicreditHUTransactionsDateFromChooser.selectMinDateFrom(false);

        assertThat(dateFrom).isBefore(OLD_DATE);
    }

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsNotPresent() {
        LocalDate dateFrom = unicreditHUTransactionsDateFromChooser.selectMinDateFrom(true);

        assertThat(dateFrom).isBefore(OLD_DATE);
    }
}
