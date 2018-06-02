package se.tink.backend.common.statistics.functions.lefttospendaverage.dto;

import com.google.common.collect.Range;
import java.text.ParseException;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Test;
import se.tink.backend.core.Statistic;
import se.tink.libraries.date.ThreadSafeDateFormat;
import static org.assertj.core.api.Assertions.assertThat;

public class PeriodRelativeStatisticTest {
    @Test(expected = IllegalStateException.class)
    public void doesntAllowLessThan0Percentage() throws ParseException {
        Date start = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-01");
        Date end = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-31");
        Range<DateTime> periodRange = Range.closed(
                new DateTime(start).withZoneRetainFields(DateTimeZone.UTC),
                new DateTime(end).withZoneRetainFields(DateTimeZone.UTC));

        Statistic statistic = new Statistic();
        statistic.setDescription("2014-12-31");
        DateStatistic lessThanMinimum = new DateStatistic(statistic);

        new PeriodRelativeStatistic(periodRange, lessThanMinimum);
    }

    @Test(expected = IllegalStateException.class)
    public void doesntAllowMoreThan1Percentage() throws ParseException {
        Date start = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-01");
        Date end = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-31");
        Range<DateTime> periodRange = Range.closed(
                new DateTime(start).withZoneRetainFields(DateTimeZone.UTC),
                new DateTime(end).withZoneRetainFields(DateTimeZone.UTC));

        Statistic statistic = new Statistic();
        statistic.setDescription("2015-02-01");
        DateStatistic lessThanMinimum = new DateStatistic(statistic);

        new PeriodRelativeStatistic(periodRange, lessThanMinimum);
    }

    @Test
    public void is0ForStartOfPeriod() throws ParseException {
        Date start = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-01");
        Date end = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-31");
        Range<DateTime> periodRange = Range.closed(
                new DateTime(start).withZoneRetainFields(DateTimeZone.UTC),
                new DateTime(end).withZoneRetainFields(DateTimeZone.UTC));

        Statistic statistic = new Statistic();
        statistic.setDescription("2015-01-01");
        DateStatistic lessThanMinimum = new DateStatistic(statistic);

        PeriodRelativeStatistic periodRelativeStatistic = new PeriodRelativeStatistic(periodRange, lessThanMinimum);

        assertThat(periodRelativeStatistic.getPeriodRelativePercentage()).isEqualTo(0.0);
    }

    @Test
    public void is1ForEndOfPeriod() throws ParseException {
        Date start = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-01");
        Date end = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-31");
        Range<DateTime> periodRange = Range.closed(
                new DateTime(start).withZoneRetainFields(DateTimeZone.UTC),
                new DateTime(end).withZoneRetainFields(DateTimeZone.UTC));

        Statistic statistic = new Statistic();
        statistic.setDescription("2015-01-31");
        DateStatistic lessThanMinimum = new DateStatistic(statistic);

        PeriodRelativeStatistic periodRelativeStatistic = new PeriodRelativeStatistic(periodRange, lessThanMinimum);

        assertThat(periodRelativeStatistic.getPeriodRelativePercentage()).isEqualTo(1.0);
    }

    @Test
    public void is05ForMiddleOfPeriod() throws ParseException {
        Date start = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-01");
        Date end = ThreadSafeDateFormat.FORMATTER_DAILY.parse("2015-01-03");
        Range<DateTime> periodRange = Range.closed(
                new DateTime(start).withZoneRetainFields(DateTimeZone.UTC),
                new DateTime(end).withZoneRetainFields(DateTimeZone.UTC));

        Statistic statistic = new Statistic();
        statistic.setDescription("2015-01-02");
        DateStatistic lessThanMinimum = new DateStatistic(statistic);

        PeriodRelativeStatistic periodRelativeStatistic = new PeriodRelativeStatistic(periodRange, lessThanMinimum);

        assertThat(periodRelativeStatistic.getPeriodRelativePercentage()).isEqualTo(0.5);
    }
}