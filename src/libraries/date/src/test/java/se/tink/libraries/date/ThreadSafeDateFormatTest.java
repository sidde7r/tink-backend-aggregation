package se.tink.libraries.date;

import java.time.LocalDate;
import java.time.LocalDateTime;
import junitparams.JUnitParamsRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ThreadSafeDateFormatTest {

    @Test
    public void shouldFormatLocalDate() {
        // given
        LocalDate date = LocalDate.of(2019, 9, 14);

        // then
        Assert.assertEquals("2019-09-14", ThreadSafeDateFormat.FORMATTER_DAILY.format(date));
        Assert.assertEquals("14/09/2019", ThreadSafeDateFormat.FORMATTER_DD_MM_YYYY.format(date));
        Assert.assertEquals("19-09-14", ThreadSafeDateFormat.FORMATTER_DAILY_COMPACT.format(date));
    }

    @Test
    public void shouldParseStringToLocalDate() {
        // given
        LocalDate expectedDate = LocalDate.of(2021, 12, 24);

        // then
        Assert.assertEquals(
                expectedDate, ThreadSafeDateFormat.FORMATTER_DAILY.parseToLocalDate("2021-12-24"));
        Assert.assertEquals(
                expectedDate,
                ThreadSafeDateFormat.FORMATTER_DD_MM_YYYY.parseToLocalDate("24/12/2021"));
        Assert.assertEquals(
                expectedDate,
                ThreadSafeDateFormat.FORMATTER_DAILY_COMPACT.parseToLocalDate("21-12-24"));
    }

    @Test
    public void shouldFormatLocalDateTime() {
        // given
        LocalDateTime date = LocalDateTime.of(2019, 3, 28, 14, 33, 48, 123456789);

        // then
        Assert.assertEquals("2019-03-28", ThreadSafeDateFormat.FORMATTER_DAILY.format(date));
        Assert.assertEquals("28/03/2019", ThreadSafeDateFormat.FORMATTER_DD_MM_YYYY.format(date));
        Assert.assertEquals("19-03-28", ThreadSafeDateFormat.FORMATTER_DAILY_COMPACT.format(date));
        Assert.assertEquals(
                "2019-03-28T14:33:48", ThreadSafeDateFormat.FORMATTER_SECONDS_T.format(date));
        Assert.assertEquals(
                "2019-03-28T14:33:48Z",
                ThreadSafeDateFormat.FORMATTER_SECONDS_T_WITH_TIMEZONE.format(date));
        Assert.assertEquals(
                "2019-03-28T14:33:48.123Z",
                ThreadSafeDateFormat.FORMATTER_MILLISECONDS_WITHOUT_TIMEZONE.format(date));
    }

    @Test
    public void shouldParseStringToLocalDateTime() {
        // given
        LocalDateTime expectedDateWithSecondPrecision = LocalDateTime.of(2019, 3, 28, 14, 33, 48);
        LocalDateTime expectedDateWithMinutePrecision = LocalDateTime.of(2019, 3, 28, 14, 33);

        // then
        Assert.assertEquals(
                expectedDateWithSecondPrecision,
                ThreadSafeDateFormat.FORMATTER_SECONDS_T.parseToLocalDateTime(
                        "2019-03-28T14:33:48"));
        Assert.assertEquals(
                expectedDateWithSecondPrecision,
                ThreadSafeDateFormat.FORMATTER_SECONDS_T_WITH_TIMEZONE.parseToLocalDateTime(
                        "2019-03-28T14:33:48Z"));
        Assert.assertEquals(
                expectedDateWithMinutePrecision,
                ThreadSafeDateFormat.FORMATTER_MINUTES.parseToLocalDateTime("2019-03-28 14:33"));
    }
}
