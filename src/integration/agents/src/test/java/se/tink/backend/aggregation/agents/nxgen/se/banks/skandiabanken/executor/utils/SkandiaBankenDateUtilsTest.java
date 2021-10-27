package se.tink.backend.aggregation.agents.nxgen.se.banks.skandiabanken.executor.utils;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import org.junit.Before;
import org.junit.Test;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class SkandiaBankenDateUtilsTest {

    private static final ZoneId ZONE_ID = ZoneId.of("Europe/Stockholm");
    private static final Locale LOCALE = new Locale("sv", "SE");
    private static final ThreadSafeDateFormat FORMATTER_MILLISECONDS_WITH_TIMEZONE =
            new ThreadSafeDateFormat(
                    "yyyy-MM-dd'T'HH:mm:ss.SSSZ", LOCALE, TimeZone.getTimeZone(ZONE_ID));

    private SkandiaBankenDateUtils objectUnderTest;

    @Before
    public void setUp() {
        objectUnderTest = new SkandiaBankenDateUtils();
    }

    @Test
    public void shouldReturnTransferDueDateAtMidnightWhenSetWithAnyTime() {
        // given
        final long epochTimestamp = 1637860079; // Thursday, 25 November 2021 17:07:59 UTC
        final Instant instant = Instant.ofEpochSecond(epochTimestamp);
        final Date transferDueDate = Date.from(instant);

        // when
        final Date transferDate = objectUnderTest.getTransferDateForBgPg(transferDueDate);

        // then
        assertThat(FORMATTER_MILLISECONDS_WITH_TIMEZONE.format(transferDate))
                .isEqualTo("2021-11-25T00:00:00.000+0100");
    }

    @Test
    public void shouldReturnTransferDueDateAtMidnightWhenSetWithMidnight() {
        // given
        final long epochTimestamp = 1637967600; // Friday, 26 November 2021 23:00:00 UTC
        final Instant instant = Instant.ofEpochSecond(epochTimestamp);
        final Date transferDueDate = Date.from(instant);

        // when
        final Date transferDate = objectUnderTest.getTransferDateForBgPg(transferDueDate);

        // then
        assertThat(FORMATTER_MILLISECONDS_WITH_TIMEZONE.format(transferDate))
                .isEqualTo("2021-11-27T00:00:00.000+0100");
    }

    @Test
    public void shouldReturnTransferDueDateAtMidnightWhenDateIsDst() {
        // given
        final long epochTimestamp = 1634637600; // Tuesday, 19 October 2021 10:00:00 UTC
        final Instant instant = Instant.ofEpochSecond(epochTimestamp);
        final Date transferDueDate = Date.from(instant);

        // when
        final Date transferDate = objectUnderTest.getTransferDateForBgPg(transferDueDate);

        // then
        assertThat(FORMATTER_MILLISECONDS_WITH_TIMEZONE.format(transferDate))
                .isEqualTo("2021-10-19T00:00:00.000+0200");
    }

    @Test
    public void shouldReturnTodaysDateAtMidnightIfBusinessDayAndBeforeCutOff() {
        // given
        final long epochTimeStamp = 1637815500; // Thursday, 25 November 2021 04:45:00 UTC
        final Instant instant = Instant.ofEpochSecond(epochTimeStamp);
        SkandiaBankenDateUtils.setClockForTesting(Clock.fixed(instant, ZONE_ID));

        // when
        final Date transferDate = objectUnderTest.getTransferDateForBgPg(null);

        // then
        assertThat(FORMATTER_MILLISECONDS_WITH_TIMEZONE.format(transferDate))
                .isEqualTo("2021-11-25T00:00:00.000+0100");
    }

    @Test
    public void shouldReturnNextBusinessDayAtMidnightIfBusinessDayAndAfterCutOff() {
        final long epochTimestamp = 1637837100; // Thursday, 25 November 2021 10:45:00 UTC
        final Instant instant = Instant.ofEpochSecond(epochTimestamp);
        SkandiaBankenDateUtils.setClockForTesting(Clock.fixed(instant, ZONE_ID));

        // when
        final Date transferDate = objectUnderTest.getTransferDateForBgPg(null);

        // then
        assertThat(FORMATTER_MILLISECONDS_WITH_TIMEZONE.format(transferDate))
                .isEqualTo("2021-11-26T00:00:00.000+0100");
    }

    @Test
    public void shouldReturnNextBusinessDayAtMidnightIfNotBusinessDay() {
        final long epochTimeStamp = 1637989505; // Saturday, 27 November 2021 05:05:05 UTC
        final Instant instant = Instant.ofEpochSecond(epochTimeStamp);
        SkandiaBankenDateUtils.setClockForTesting(Clock.fixed(instant, ZONE_ID));

        // when
        final Date transferDate = objectUnderTest.getTransferDateForBgPg(null);

        // then
        assertThat(FORMATTER_MILLISECONDS_WITH_TIMEZONE.format(transferDate))
                .isEqualTo("2021-11-29T00:00:00.000+0100");
    }
}
