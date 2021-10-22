package se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.date.range;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.Period;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.ConstantLocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.date.LocalDateTimeSource;
import se.tink.backend.aggregation.nxgen.controllers.refresh.transaction.pagination.TransactionPaginationHelper;
import se.tink.backend.aggregation.nxgen.core.account.Account;

@RunWith(MockitoJUnitRunner.class)
public class DateRangeCalculatorTest {

    @Mock private TransactionPaginationHelper helper;
    @Mock private Account account;

    private LocalDateTimeSource constantDateTimeSource;
    private DateRangeCalculator<Account> calculator;

    @Before
    public void setUp() throws Exception {
        constantDateTimeSource = new ConstantLocalDateTimeSource();
        calculator = new DateRangeCalculator<>(constantDateTimeSource, ZoneOffset.UTC, helper);
    }

    @Test
    public void shouldCalculateToDateAsNow() {
        // given
        OffsetDateTime now = constantDateTimeSource.now().atOffset(ZoneOffset.UTC);

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
    public void shouldCalculateFromAsStartOfTheDayCorrectly() {
        // given
        OffsetDateTime toDateTime =
                LocalDate.of(2021, 1, 15).atStartOfDay().atOffset(ZoneOffset.UTC);
        Period range = Period.ofWeeks(1);

        // when
        OffsetDateTime fromDateTime = calculator.calculateFromAsStartOfTheDay(toDateTime, range);

        // then
        assertThat(fromDateTime)
                .isEqualTo(LocalDate.of(2021, 1, 8).atStartOfDay().atOffset(ZoneOffset.UTC));
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

    @Test
    public void shouldApplyCertainDateLimit() {
        // given
        OffsetDateTime proposedFromDateTime =
                LocalDate.of(2021, 1, 15).atStartOfDay().atOffset(ZoneOffset.UTC);
        given(helper.getTransactionDateLimit(account))
                .willReturn(optionalDate(2021, 1, 20, 13, 0, 0, ZoneOffset.UTC));

        // when
        OffsetDateTime fromDateTime =
                calculator.applyCertainDateLimit(account, proposedFromDateTime);

        // then
        assertThat(fromDateTime)
                .isEqualTo(LocalDate.of(2021, 1, 20).atStartOfDay().atOffset(ZoneOffset.UTC));
    }

    @Test
    public void shouldNotApplyCertainDateLimit() {
        // given
        OffsetDateTime proposedFromDateTime =
                LocalDate.of(2021, 1, 15).atStartOfDay().atOffset(ZoneOffset.UTC);
        given(helper.getTransactionDateLimit(account)).willReturn(Optional.empty());

        // when
        OffsetDateTime fromDateTime =
                calculator.applyCertainDateLimit(account, proposedFromDateTime);

        // then
        assertThat(fromDateTime).isEqualTo(proposedFromDateTime);
    }

    private Optional<Date> optionalDate(
            int year, int month, int day, int hour, int min, int sec, ZoneOffset offset) {
        return Optional.of(
                Date.from(
                        LocalDate.of(year, month, day)
                                .atTime(hour, min, sec)
                                .atOffset(offset)
                                .toInstant()));
    }
}
