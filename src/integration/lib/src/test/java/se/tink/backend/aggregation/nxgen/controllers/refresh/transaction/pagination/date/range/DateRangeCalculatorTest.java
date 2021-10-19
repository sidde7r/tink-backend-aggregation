package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import org.junit.Before;
import org.junit.Test;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;

public class DateRangeCalculatorTest {

    private DateRangeCalculator calculator;
    private LocalDateTimeSource localDateTimeSource;

    @Before
    public void setUp() throws Exception {
        localDateTimeSource = new ConstantLocalDateTimeSource();
        calculator = new DateRangeCalculator(new ConstantLocalDateTimeSource(), ZoneOffset.UTC);
    }

    @Test
    public void shouldCalculateToDateAsNow() {
        // given
        OffsetDateTime now = localDateTimeSource.now().atOffset(ZoneOffset.UTC);

        // when
        OffsetDateTime toDateTime = calculator.calculateTo(null);

        // then
        assertThat(toDateTime).isEqualTo(now);
    }

    @Test
    public void shouldCalculateToDateCorrectly() {
        // given
        OffsetDateTime fromDateTime =
                LocalDate.of(2021, 1, 15).atStartOfDay().atOffset(ZoneOffset.UTC);

        // when
        OffsetDateTime toDateTime = calculator.calculateTo(fromDateTime);

        // then
        assertThat(toDateTime)
                .isEqualTo(
                        LocalDateTime.of(2021, 1, 14, 23, 59, 59, 999999999)
                                .atOffset(ZoneOffset.UTC));
    }

    @Test
    public void shouldCalculateFromDateCorrectlyByApplyingLimit() {
        // given
        OffsetDateTime toDateTime =
                LocalDate.of(2021, 1, 15).atStartOfDay().atOffset(ZoneOffset.UTC);
        Period range = Period.ofWeeks(1);
        OffsetDateTime limit = LocalDate.of(2021, 1, 14).atStartOfDay().atOffset(ZoneOffset.UTC);

        // when
        OffsetDateTime fromDateTime =
                calculator.calculateFromAsStartOfTheDayWithLimit(toDateTime, range, limit);

        // then
        assertThat(fromDateTime).isEqualTo(limit);
    }

    @Test
    public void shouldCalculateFromDateCorrectlyWithoutApplyingLimit() {
        // given
        OffsetDateTime toDateTime =
                LocalDate.of(2021, 1, 15).atStartOfDay().atOffset(ZoneOffset.UTC);
        Period range = Period.ofWeeks(1);
        OffsetDateTime limit = LocalDate.of(2019, 1, 1).atStartOfDay().atOffset(ZoneOffset.UTC);

        // when
        OffsetDateTime fromDateTime =
                calculator.calculateFromAsStartOfTheDayWithLimit(toDateTime, range, limit);

        // then
        assertThat(fromDateTime)
                .isEqualTo(LocalDate.of(2021, 1, 8).atStartOfDay().atOffset(ZoneOffset.UTC));
    }
}
