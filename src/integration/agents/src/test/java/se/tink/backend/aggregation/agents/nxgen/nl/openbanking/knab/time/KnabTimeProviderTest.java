package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.time;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

@RunWith(MockitoJUnitRunner.class)
public class KnabTimeProviderTest {

    private final ZoneId zoneId = ZoneId.of("GMT");

    private final ZonedDateTime zonedDateTime =
            ZonedDateTime.of(2000, 11, 22, 3, 4, 5, 678, zoneId);

    @Mock private LocalDateTimeSource localDateTimeSource;

    @InjectMocks private KnabTimeProvider dateProvider;

    @Before
    public void setUp() {
        when(localDateTimeSource.now(ZoneId.systemDefault()))
                .thenReturn(zonedDateTime.toLocalDateTime());
        when(localDateTimeSource.getInstant(zoneId)).thenReturn(zonedDateTime.toInstant());
    }

    @Test
    public void shouldReturnDate() {
        // when
        LocalDate date = dateProvider.date();

        // then
        assertThat(date).isEqualTo(zonedDateTime.toLocalDate());
    }

    @Test
    public void shouldReturnFormattedDate() {
        // when
        String formatted = dateProvider.formatted();

        // then
        assertThat(formatted).isEqualTo("Wed, 22 Nov 2000 03:04:05 GMT");
    }
}
