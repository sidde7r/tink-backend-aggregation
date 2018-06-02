package se.tink.backend.common.statistics.functions.lefttospendaverage.interpolation;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.common.SwedishTimeRule;
import se.tink.backend.common.statistics.functions.lefttospendaverage.dto.DateStatistic;
import se.tink.backend.common.statistics.functions.lefttospendaverage.dto.PeriodRelativeStatistic;
import se.tink.backend.core.Statistic;
import static org.assertj.core.api.Assertions.assertThat;

public class PeriodRelativeMeanInterpolatorTest {
    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test
    public void startOfAveragePeriodIsExpected() throws ParseException {
        Collection<Collection<PeriodRelativeStatistic>> relativeStatisticsByPeriod = stubRelativeStatisticsByPeriod();

        PeriodRelativeMeanInterpolator periodRelativeMeanInterpolator = new PeriodRelativeMeanInterpolator();
        periodRelativeMeanInterpolator.interpolateCompletePeriods(relativeStatisticsByPeriod);
        double mean = periodRelativeMeanInterpolator.getMean(0);

        assertThat(mean).isEqualTo(1500.0);
    }

    @Test
    public void middleOfAveragePeriodIsExpected() throws ParseException {
        Collection<Collection<PeriodRelativeStatistic>> relativeStatisticsByPeriod = stubRelativeStatisticsByPeriod();

        PeriodRelativeMeanInterpolator periodRelativeMeanInterpolator = new PeriodRelativeMeanInterpolator();
        periodRelativeMeanInterpolator.interpolateCompletePeriods(relativeStatisticsByPeriod);
        double mean = periodRelativeMeanInterpolator.getMean(0.5);

        assertThat(mean).isEqualTo(150.0);
    }

    @Test
    public void endOfAveragePeriodIsExpected() throws ParseException {
        Collection<Collection<PeriodRelativeStatistic>> relativeStatisticsByPeriod = stubRelativeStatisticsByPeriod();

        PeriodRelativeMeanInterpolator periodRelativeMeanInterpolator = new PeriodRelativeMeanInterpolator();
        periodRelativeMeanInterpolator.interpolateCompletePeriods(relativeStatisticsByPeriod);
        double mean = periodRelativeMeanInterpolator.getMean(1);

        assertThat(mean).isEqualTo(15.0);
    }

    @Test
    public void isContinousBetweenTwoDatapoints() throws ParseException {
        Collection<Collection<PeriodRelativeStatistic>> relativeStatisticsByPeriod = stubRelativeStatisticsByPeriod();

        PeriodRelativeMeanInterpolator periodRelativeMeanInterpolator = new PeriodRelativeMeanInterpolator();
        periodRelativeMeanInterpolator.interpolateCompletePeriods(relativeStatisticsByPeriod);
        double mean1 = periodRelativeMeanInterpolator.getMean(0.251);
        double mean2 = periodRelativeMeanInterpolator.getMean(0.252);
        double mean3 = periodRelativeMeanInterpolator.getMean(0.253);

        // According to setup this should be the effect of the data points
        assertThat(mean1).isLessThan(mean2);
        assertThat(mean2).isLessThan(mean3);
    }

    /**
     * Construct datapoint curves that we can interpret with tests from two periods with different lengths
     */
    private Collection<Collection<PeriodRelativeStatistic>> stubRelativeStatisticsByPeriod() throws ParseException {
        Range<DateTime> period1 = getRange("2015-04-01", "2015-04-06");
        Range<DateTime> period2 = getRange("2015-05-01", "2015-05-08");

        Collection<PeriodRelativeStatistic> period1Statistics = ImmutableList.of(
                stubPeriodRelativeStatistic(period1, "2015-04-01", 2000.0),
                stubPeriodRelativeStatistic(period1, "2015-04-02", 2.0),
                stubPeriodRelativeStatistic(period1, "2015-04-03", 200.0),
                stubPeriodRelativeStatistic(period1, "2015-04-04", 200.0),
                stubPeriodRelativeStatistic(period1, "2015-04-05", 2.0),
                stubPeriodRelativeStatistic(period1, "2015-04-06", 20.0)
        );

        Collection<PeriodRelativeStatistic> period2Statistics = ImmutableList.of(
                stubPeriodRelativeStatistic(period2, "2015-05-01", 1000.0),
                stubPeriodRelativeStatistic(period2, "2015-05-02", 1.0),
                stubPeriodRelativeStatistic(period2, "2015-05-03", 1.0),
                stubPeriodRelativeStatistic(period2, "2015-05-04", 100.0),
                stubPeriodRelativeStatistic(period2, "2015-05-05", 100.0),
                stubPeriodRelativeStatistic(period2, "2015-05-06", 1.0),
                stubPeriodRelativeStatistic(period2, "2015-05-07", 1.0),
                stubPeriodRelativeStatistic(period2, "2015-05-08", 10.0)
        );

        return ImmutableList.of(period1Statistics, period2Statistics);
    }

    private PeriodRelativeStatistic stubPeriodRelativeStatistic(Range<DateTime> range, String description, double amount)
            throws ParseException {
        Statistic statistic = stubStatistic(description, amount);
        DateStatistic dateStatistic = new DateStatistic(statistic);
        return new PeriodRelativeStatistic(range, dateStatistic);
    }

    private Range<DateTime> getRange(String start, String end) {
        Date startDate = DateTime.parse(start).toDate();
        Date endDate = DateTime.parse(end).toDate();
        return Range.closed(
                new DateTime(startDate).withZoneRetainFields(DateTimeZone.UTC),
                new DateTime(endDate).withZoneRetainFields(DateTimeZone.UTC));
    }

    private Statistic stubStatistic(String description, double value) {
        Statistic statistic = new Statistic();

        statistic.setDescription(description);
        statistic.setValue(value);

        return statistic;
    }
}
