package se.tink.backend.aggregation.agents.nxgen.nl.banks.ics;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.ICSTimeProvider;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class ICSTimeProviderTest {

    private final LocalDateTimeSource localDateTimeSource = new ConstantLocalDateTimeSource();

    private final LocalDate today = localDateTimeSource.now(ZoneId.systemDefault()).toLocalDate();

    private final LocalDate fromDate = today.minusYears(3);

    private final LocalDate toAndExpiredDate = today.plusDays(89);

    private final LocalDate fallbackTransactionFromDate = today.minusYears(2).minusDays(89);

    private final LocalDate persistedTransactionFromDate = today.minusDays(10);

    private final PersistentStorage persistentStorage = new PersistentStorage();

    private final ICSTimeProvider timeProvider =
            new ICSTimeProvider(localDateTimeSource, persistentStorage);

    @Before
    public void setUp() {
        persistentStorage.put(
                StorageKeys.TRANSACTION_FROM_DATE, localDateAsDate(persistedTransactionFromDate));
    }

    @Test
    public void shouldReturnCurrentDate() {
        // when
        LocalDate today = timeProvider.now();

        // then
        assertThat(today).isEqualTo(this.today);
    }

    @Test
    public void shouldReturnFromDate() {
        // when
        Date fromDate = timeProvider.getFromDate();

        // then
        assertThat(fromDate).isEqualTo(localDateAsDate(this.fromDate));
    }

    @Test
    public void shouldReturnToAndExpiredDate() {
        // when
        Date toAndExpiredDate = timeProvider.getToAndExpiredDate();

        // then
        assertThat(toAndExpiredDate).isEqualTo(localDateAsDate(this.toAndExpiredDate));
    }

    @Test
    public void shouldReturnFormattedLogTime() {
        // when
        String loggedTimeFormatted = timeProvider.getLastLoggedTime();

        // then
        assertThat(loggedTimeFormatted).isEqualTo("Fri, 10 April 1992 00:00:00 UTC");
    }

    @Test
    public void shouldReturnTransactionFromDate() {

        // when
        Date transactionFromDate = timeProvider.getConsentTransactionDate();

        // then
        assertThat(transactionFromDate).isEqualTo(localDateAsDate(persistedTransactionFromDate));
    }

    @Test
    public void shouldReturnFallbackTransactionFromDate() {
        // given
        emptyPersistentStorage();

        // when
        Date transactionFromDate = timeProvider.getConsentTransactionDate();

        // then
        assertThat(transactionFromDate).isEqualTo(localDateAsDate(fallbackTransactionFromDate));
    }

    @Test
    public void shouldConvertDateToLocalDate() {
        // when
        LocalDate localDate = timeProvider.convertToLocalDate(localDateAsDate(today));

        // then
        assertThat(localDate).isEqualTo(today);
    }

    private void emptyPersistentStorage() {
        persistentStorage.clear();
    }

    private Date localDateAsDate(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
    }
}
