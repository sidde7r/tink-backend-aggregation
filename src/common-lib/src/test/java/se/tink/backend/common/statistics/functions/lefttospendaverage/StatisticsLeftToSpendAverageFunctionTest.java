package se.tink.backend.common.statistics.functions.lefttospendaverage;

import com.google.common.base.Objects;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Range;
import java.util.List;
import org.joda.time.DateTime;
import org.junit.Rule;
import org.junit.Test;
import se.tink.backend.common.SwedishTimeRule;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.core.UserProfile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.createLTSA;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.createLTSA_Daily;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.createLTSA_FebruaryPeriod;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.createLTSA_MonthlyDecember;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.createLTSA_MonthlyFebruary;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.createLTSA_MonthlyJanuary;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.createLTSA_MonthlyMarch;
import static se.tink.backend.common.statistics.functions.stubs.StatisticsStub.stubBuilder;

public class StatisticsLeftToSpendAverageFunctionTest {

    /**
     * General notes on tests for this implementation:
     * 1. The implementation (when this was written) uses interpolation and sampling,
     * which means we cannot rely on exact values being delivered exactly the same in the resulting object as one
     * would imagine. We need to check boundaries surrounding the value we want to test to see if it is reasonable.
     * 2. Since the Function needs complete months to be able to give any reasonable left-to-spend-average it just calculates
     * statistics for months between the first seen statistics object and the last seen
     */
    private static int MAX_AVARAGE_PERIODS = 6;

    @Rule
    public SwedishTimeRule timeRule = new SwedishTimeRule();

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsWhenPassedNullUser() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, null, MAX_AVARAGE_PERIODS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsWhenPassedNullResolutionType() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(null,
                mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testThrowsWhenPassedNullStatistics() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        function.apply(null);
    }

    @Test
    public void testDoesntCalculateStatisticsWhenInsufficientCompletePeriods() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyDecember("05", 0.0))
                .add(createLTSA_MonthlyJanuary("06", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        assertThat(averages).hasSize(62);

        for (Statistic average : averages) {
            assertThat(average.getValue()).isEqualTo(0.);
        }
    }

    @Test
    public void testCalculatesStatisticsWhenFirstAndLastDateIsBeforeAndAfterACompletePeriod() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyDecember("31", 0.0))
                .add(createLTSA_MonthlyFebruary("01", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        assertThat(averages).isNotEmpty();
    }

    @Test
    public void testStatisticsOnSameDayIsMergedWithMean() {
        // For statistics to be calculated, the function checks first and last statistics objects
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyDecember("31", 0.0))
                .add(createLTSA_MonthlyJanuary("06", 0.0))
                .add(createLTSA_MonthlyJanuary("06", 20.0))
                .add(createLTSA_MonthlyJanuary("06", -30.0))
                .add(createLTSA_MonthlyFebruary("01", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        // Since the calculation of the function uses sampling and interpolation with derivatives,
        // we cannot rely on "2015-01-06" being exactly the value -10. But the surrounding values should be expected
        // regarding the merged mean
        List<Statistic> expected = stubBuilder()
                .add(createLTSA_Daily("2015-02", "2015-02-04", 0.0))
                .add(createLTSA_Daily("2015-02", "2015-02-08", -10.0))
                .toList();

        assertThat(averages).usingFieldByFieldElementComparator().containsAll(expected);
    }

    @Test
    public void testStatisticsAccumulatesToShowLeftToSpend() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyDecember("31", 0.0))
                .add(createLTSA_MonthlyJanuary("05", 100.0))
                .add(createLTSA_MonthlyJanuary("15", -30.0))
                .add(createLTSA_MonthlyJanuary("25", -30.0))
                .add(createLTSA_MonthlyFebruary("01", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        List<Statistic> expected = stubBuilder()
                .add(createLTSA_Daily("2015-02", "2015-02-10", 100.0))
                .add(createLTSA_Daily("2015-02", "2015-02-20", 70.0))
                .add(createLTSA_Daily("2015-02", "2015-02-28", 40.0))
                .toList();

        assertThat(averages).usingFieldByFieldElementComparator().containsAll(expected);
    }

    @Test
    public void testDaysWithoutTransactionsBetweenStartAndEndShouldHaveAverages() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyDecember("31", 0.0))
                .add(createLTSA_MonthlyJanuary("05", 100.0))
                .add(createLTSA_MonthlyJanuary("15", 100.0))
                .add(createLTSA_MonthlyJanuary("25", -300.0))
                .add(createLTSA_MonthlyFebruary("01", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        ImmutableList<Statistic> sortedFebruaryAverages = getSortedPeriod(averages, "2015-02");
        assertThat(sortedFebruaryAverages).hasSize(28);

        Range<String> firstInterval = Range.closed("2015-02-01", "2015-02-03");
        Range<String> secondInterval = Range.closed("2015-02-05", "2015-02-12");
        Range<String> thirdInterval = Range.closed("2015-02-14", "2015-02-21");
        Range<String> fourthInterval = Range.closed("2015-02-23", "2015-02-28");

        int daysInFirstInterval = 3;
        int daysInSecondInterval = 8;
        int daysInThirdInterval = 8;
        int daysInFourthInterval = 6;

        for (Statistic statistic : sortedFebruaryAverages) {
            if (firstInterval.contains(statistic.getDescription())) {
                assertThat(statistic.getValue()).isEqualTo(0.0);
                daysInFirstInterval--;
            } else if (secondInterval.contains(statistic.getDescription())) {
                assertThat(statistic.getValue()).isEqualTo(100.0);
                daysInSecondInterval--;
            } else if (thirdInterval.contains(statistic.getDescription())) {
                assertThat(statistic.getValue()).isEqualTo(200.0);
                daysInThirdInterval--;
            } else if (fourthInterval.contains(statistic.getDescription())) {
                assertThat(statistic.getValue()).isEqualTo(-100.0);
                daysInFourthInterval--;
            }
        }

        assertThat(daysInFirstInterval).isEqualTo(0);
        assertThat(daysInSecondInterval).isEqualTo(0);
        assertThat(daysInThirdInterval).isEqualTo(0);
        assertThat(daysInFourthInterval).isEqualTo(0);
    }

    @Test
    public void testLastPeriodSeenShouldHaveAveragesForAllDays() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyDecember("31", 0.0))
                .add(createLTSA_MonthlyJanuary("05", 100.0))
                .add(createLTSA_MonthlyFebruary("03", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);
        ImmutableList<Statistic> lastPeriodSeenAverages = getSortedPeriod(averages, "2015-02");

        assertThat(lastPeriodSeenAverages).hasSize(28);
    }

    @Test
    public void testCalculatesFromFirstDateInFirstSeenPeriodToLastDateInLastSeenPeriod() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyDecember("31", 0.0))
                .add(createLTSA_MonthlyJanuary("05", 100.0))
                .add(createLTSA_MonthlyFebruary("03", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        ImmutableList<Statistic> december = getSortedPeriod(averages, "2014-12");
        ImmutableList<Statistic> january = getSortedPeriod(averages, "2015-01");
        ImmutableList<Statistic> february = getSortedPeriod(averages, "2015-02");

        assertThat(averages).hasSize(december.size() + january.size() + february.size());
        assertThat(december).hasSize(31);
        assertThat(january).hasSize(31);
        assertThat(february).hasSize(28);
    }

    @Test
    public void testStatisticsOrderShouldNotMatter() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyFebruary("03", 0.0))
                .add(createLTSA_MonthlyJanuary("05", 100.0))
                .add(createLTSA_MonthlyDecember("31", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        ImmutableList<Statistic> december = getSortedPeriod(averages, "2014-12");
        ImmutableList<Statistic> january = getSortedPeriod(averages, "2015-01");
        ImmutableList<Statistic> february = getSortedPeriod(averages, "2015-02");

        assertThat(averages).hasSize(december.size() + january.size() + february.size());
        assertThat(december).hasSize(31);
        assertThat(january).hasSize(31);
        assertThat(february).hasSize(28);
    }

    @Test
    public void testPeriodsWithoutStatisticsBetweenOtherPeriodsAreAdded() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyJanuary("31", 200.0))
                .add(createLTSA_MonthlyMarch("01", 100.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        ImmutableList<Statistic> february = getSortedPeriod(averages, "2015-02");

        assertThat(february).hasSize(28);
    }

    @Test
    public void testDiffersSameDayOccurringTwiceInSamePeriodButDifferentMonths() {
        User userWithAdjustedDay = mockUserWithAdjustedDay(25);
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY_ADJUSTED, userWithAdjustedDay, MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyJanuary("22", 0.0))
                .add(createLTSA_FebruaryPeriod("01", "23", 100.0)) // Last business day before adjusted day
                .add(createLTSA_FebruaryPeriod("02", "23", 100.0)) // On a regular business day
                .add(createLTSA_MonthlyMarch("01", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        List<Statistic> expected = stubBuilder()
                .add(createLTSA_Daily("2015-03", "2015-02-25", 100.0))
                .add(createLTSA_Daily("2015-03", "2015-03-24", 200.0)) // Accumulated value
                .toList();

        assertThat(averages).usingFieldByFieldElementComparator().containsAll(expected);
    }

    @Test
    public void testResetsAccumulatedSumAfterAdjustedDay() {
        User userWithAdjustedDay = mockUserWithAdjustedDay(28);
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY_ADJUSTED, userWithAdjustedDay, MAX_AVARAGE_PERIODS);
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA_MonthlyDecember("22", 0.0))
                .add(createLTSA_MonthlyJanuary("25", 1000.0))
                .add(createLTSA_MonthlyFebruary("25", 1000.0))
                .add(createLTSA_MonthlyMarch("01", 0.0))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);
        Statistic lastStatisticInFebruary = getSortedPeriod(averages, "2015-02").stream()
                .reduce((a, b) -> b) // Get the last value. Unfortunately there is no ".last()" stream method.
                .orElse(null);
        Statistic firstStatisticInMarch = getSortedPeriod(averages, "2015-03").stream()
                .findFirst().orElse(null);

        assertThat(lastStatisticInFebruary.getDescription()).isEqualTo("2015-02-26");
        assertThat(lastStatisticInFebruary.getValue()).isEqualTo(1000.0);

        assertThat(firstStatisticInMarch.getDescription()).isEqualTo("2015-02-27");
        assertThat(firstStatisticInMarch.getValue()).isEqualTo(0.0);
    }

    @Test
    public void testExpectedOneDayForDatesLessThan24HoursBetween() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);

        DateTime start = new DateTime(2016, 10, 20, 13, 0);
        DateTime end = new DateTime(2016, 10, 21, 5, 0);

        int actResult = function.daysBetween(start, end);
        int expResult = 1;

        assertThat(actResult).isEqualTo(expResult);
    }

    @Test
    public void testExpectedOneDayForDatesMoreThan24HoursBetween() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);

        DateTime start = new DateTime(2016, 10, 20, 5, 0);
        DateTime end = new DateTime(2016, 10, 21, 13, 0);

        int actResult = function.daysBetween(start, end);
        int expResult = 1;

        assertThat(actResult).isEqualTo(expResult);
    }

    @Test
    public void testDifferentStatisticForDifferentMonths() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), MAX_AVARAGE_PERIODS);

        List<Statistic> statistics = stubBuilder()
                .add(createLTSA(2016, 1, 1, 5d))
                .add(createLTSA(2016, 1, 24, 15d))
                .add(createLTSA(2016, 2, 6, 50d))
                .add(createLTSA(2016, 2, 28, 9d))
                .add(createLTSA(2016, 3, 4, 40d))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        assertThat(averages).isNotEmpty();
        ImmutableList<Statistic> january = getSortedPeriod(averages, "2016-01");
        ImmutableList<Statistic> february = getSortedPeriod(averages, "2016-02");
        ImmutableList<Statistic> march = getSortedPeriod(averages, "2016-03");

        assertThat(averages).hasSize(january.size() + february.size() + march.size());
        assertThat(january).hasSize(31);
        assertThat(february).hasSize(29);
        assertThat(march).hasSize(31);
        assertThat(january).isNotEqualTo(march);
    }

    @Test
    public void testAverageBasedOnTwoMonths() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), 2);

        List<Statistic> statistics = stubBuilder()
                .add(createLTSA(2015, 12, 1, 15d))
                .add(createLTSA(2015, 12, 24, 15d))
                .add(createLTSA(2016, 1, 1, 5d))
                .add(createLTSA(2016, 1, 24, 15d))
                .add(createLTSA(2016, 2, 1, 45d))
                .add(createLTSA(2016, 2, 24, 9d))
                .add(createLTSA(2016, 3, 4, 40d))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        assertThat(averages).isNotEmpty();
        ImmutableList<Statistic> january = getSortedPeriod(averages, "2016-01");
        ImmutableList<Statistic> february = getSortedPeriod(averages, "2016-02");
        ImmutableList<Statistic> march = getSortedPeriod(averages, "2016-03");

        assertThat(averages).hasSize(31 + 31 + 29 + 31);
        assertThat(january).hasSize(31);
        assertThat(february).hasSize(29);
        assertThat(march).hasSize(31);
        assertThat(january).isNotEqualTo(march);

        List<Statistic> expected = stubBuilder()
                .add(createLTSA_Daily("2016-03", "2016-03-22", 25))
                .add(createLTSA_Daily("2016-03", "2016-03-01", 25))
                .add(createLTSA_Daily("2016-03", "2016-03-30", 37.0))
                .add(createLTSA_Daily("2016-02", "2016-02-01", 10.0))
                .add(createLTSA_Daily("2016-02", "2016-02-24", 25.0))
                .add(createLTSA_Daily("2016-02", "2016-02-28", 25.0))
                .add(createLTSA_Daily("2016-01", "2016-01-01", 15.0))
                .add(createLTSA_Daily("2016-01", "2016-01-25", 30.0))
                .add(createLTSA_Daily("2016-01", "2016-01-28", 30.0))
                .add(createLTSA_Daily("2015-12", "2015-12-01", 0.0))
                .add(createLTSA_Daily("2015-12", "2015-12-24", 0.0))
                .add(createLTSA_Daily("2015-12", "2015-12-28", 0.0))
                .toList();

        assertThat(averages).usingFieldByFieldElementComparator().containsAll(expected);
    }

    @Test
    public void testAverageBasedOnTwoMonthsMonthlyAdjusted() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY_ADJUSTED, mockUserWithAdjustedDay(25), 2);

        List<Statistic> statistics = stubBuilder()
                .add(createLTSA(2015, 11, 15, 150d))
                .add(createLTSA(2015, 12, 1, 15d))
                .add(createLTSA(2015, 12, 20, 15d))
                .add(createLTSA(2016, 1, 1, 5d))
                .add(createLTSA(2016, 1, 20, 15d))
                .add(createLTSA(2016, 2, 1, 45d))
                .add(createLTSA(2016, 2, 20, 9d))
                .add(createLTSA(2016, 3, 4, 40d))
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        assertThat(averages).isNotEmpty();
        ImmutableList<Statistic> january = getSortedPeriod(averages, "2016-01");
        ImmutableList<Statistic> february = getSortedPeriod(averages, "2016-02");
        ImmutableList<Statistic> march = getSortedPeriod(averages, "2016-03");

        assertThat(averages).hasSize(30 + 31 + 33 + 31 + 28);
        assertThat(january).hasSize(33);
        assertThat(february).hasSize(31);
        assertThat(march).hasSize(28);
        assertThat(january).isNotEqualTo(march);

        List<Statistic> expected = stubBuilder()
                .add(createLTSA_Daily("2016-03", "2016-02-25", 0))
                .add(createLTSA_Daily("2016-03", "2016-03-05", 25))
                .add(createLTSA_Daily("2016-03", "2016-03-23", 37.0))
                .add(createLTSA_Daily("2016-02", "2016-02-03", 10.0))
                .add(createLTSA_Daily("2016-02", "2016-02-22", 25.0))
                .add(createLTSA_Daily("2016-02", "2016-02-24", 25.0))
                .add(createLTSA_Daily("2016-01", "2015-12-23", 0.0))
                .add(createLTSA_Daily("2016-01", "2015-12-31", 15.0))
                .add(createLTSA_Daily("2016-01", "2016-01-24", 30.0))
                .add(createLTSA_Daily("2015-12", "2015-11-25", 0.0))
                .add(createLTSA_Daily("2015-12", "2015-12-02", 0.0))
                .add(createLTSA_Daily("2015-12", "2015-12-22", 0.0))
                .toList();

        assertThat(averages).usingFieldByFieldElementComparator().containsAll(expected);
    }

    @Test
    public void testGenerateStatisticsFor18MonthsWhenPeriodsMoreThanMaxPeriodCalculation() {
        StatisticsLeftToSpendAverageFunction function = new StatisticsLeftToSpendAverageFunction(
                ResolutionTypes.MONTHLY, mockUserWithAdjustedDay(1), 2);

        // 20 periods
        List<Statistic> statistics = stubBuilder()
                .add(createLTSA(2015, 12, 1, 15d))
                .add(createLTSA(2015, 11, 24, 15d))
                .add(createLTSA(2015, 10, 1, 5d))
                .add(createLTSA(2015, 9, 24, 15d))
                .add(createLTSA(2015, 8, 1, 45d))
                .add(createLTSA(2015, 7, 24, 9d))
                .add(createLTSA(2015, 6, 4, 40d))
                .add(createLTSA(2015, 5, 1, 15d))
                .add(createLTSA(2015, 4, 24, 15d))
                .add(createLTSA(2015, 3, 1, 5d))
                .add(createLTSA(2015, 2, 24, 15d))
                .add(createLTSA(2015, 1, 1, 45d))
                .add(createLTSA(2014, 12, 1, 15d))
                .add(createLTSA(2014, 11, 24, 15d))
                .add(createLTSA(2014, 10, 1, 5d))
                .add(createLTSA(2014, 9, 24, 15d))
                .add(createLTSA(2014, 8, 1, 45d))
                .add(createLTSA(2014, 7, 24, 9d))
                .add(createLTSA(2014, 6, 4, 40d)) // not calculate
                .add(createLTSA(2014, 5, 4, 40d)) // not calculate
                .toList();

        Iterable<Statistic> averages = function.apply(statistics);

        assertThat(averages).isNotEmpty();
        ImmutableList<Statistic> notCalculatedPeriod1 = getSortedPeriod(averages, "2014-05");
        ImmutableList<Statistic> notCalculatedPeriod2 = getSortedPeriod(averages, "2014-06");
        ImmutableList<Statistic> firstCalculatedPeriod = getSortedPeriod(averages, "2014-07");
        ImmutableList<Statistic> lastCalculatedPeriod = getSortedPeriod(averages, "2015-12");

        assertThat(averages).hasSize(549);
        assertThat(notCalculatedPeriod1).isEmpty();
        assertThat(notCalculatedPeriod2).isEmpty();
        assertThat(firstCalculatedPeriod).hasSize(31);
        assertThat(lastCalculatedPeriod).hasSize(31);
    }

    private User mockUserWithAdjustedDay(int i) {
        UserProfile userProfileMock = mock(UserProfile.class);
        when(userProfileMock.getPeriodAdjustedDay()).thenReturn(i);

        User userMock = mock(User.class);
        when(userMock.getProfile()).thenReturn(userProfileMock);

        return userMock;
    }

    private ImmutableList<Statistic> getSortedPeriod(Iterable<Statistic> averages, final String period) {
        return FluentIterable.from(averages)
                .filter(statistic -> Objects.equal(statistic.getPeriod(), period))
                .toSortedList((o1, o2) -> o1.getDescription().compareTo(o2.getDescription()));
    }
}
