package se.tink.backend.aggregation.agents.nxgen.de.openbanking.unicredit;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;

public class HvbOnlineUnicreditTransactionsDateFromChooserTest {

    private static final LocalDate NOW = new ConstantLocalDateTimeSource().now().toLocalDate();

    private HvbOnlineUnicreditTransactionsDateFromChooser
            hvbOnlineUnicreditTransactionsDateFromChooser =
                    new HvbOnlineUnicreditTransactionsDateFromChooser(
                            new ConstantLocalDateTimeSource());

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsPresent() {
        LocalDate dateFrom = hvbOnlineUnicreditTransactionsDateFromChooser.selectMinDateFrom(false);

        assertThat(dateFrom).isEqualTo(NOW.minusDays(90));
    }

    @Test
    public void shouldSelectMinDateFromWhenLastTransactionsDateIsNotPresent() {
        LocalDate dateFrom = hvbOnlineUnicreditTransactionsDateFromChooser.selectMinDateFrom(true);

        assertThat(dateFrom).isEqualTo(NOW.minusDays(750));
    }
}
