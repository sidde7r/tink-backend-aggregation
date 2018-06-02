package se.tink.backend.common.statistics.functions.lefttospendaverage;

import com.google.common.base.Objects;
import java.util.Optional;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.base.Supplier;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableCollection;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import java.text.ParseException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.function.Function;
import javax.annotation.Nullable;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.joda.time.Days;
import se.tink.backend.common.statistics.functions.lefttospendaverage.dto.DateStatistic;
import se.tink.backend.common.statistics.functions.lefttospendaverage.dto.PeriodRelativeStatistic;
import se.tink.backend.common.statistics.functions.lefttospendaverage.interpolation.PeriodRelativeMeanInterpolator;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.libraries.date.DateUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

/**
 * Reducer that sums value but set month period
 */
public class StatisticsLeftToSpendAverageFunction implements Function<Collection<Statistic>, Collection<Statistic>> {
    private static final int MAX_PERIODS_CALCULATION = 18;
    private final ResolutionTypes resolution;
    private final User user;
    private final int maxAveragePeriods;

    public StatisticsLeftToSpendAverageFunction(ResolutionTypes resolution, User user, int maxAveragePeriods) {
        Preconditions.checkArgument(resolution != null);
        Preconditions.checkArgument(user != null);

        this.resolution = resolution;
        this.user = user;
        this.maxAveragePeriods = maxAveragePeriods;
    }

    public Collection<Statistic> apply(Collection<Statistic> statistics) {
        Preconditions.checkArgument(statistics != null);

        FluentIterable<DateStatistic> dateStatistics = FluentIterable
                .from(statistics)
                .transform(STATISTIC_TO_DATESTATISTIC::apply)
                .filter(Predicates.<DateStatistic>notNull());

        dateStatistics = filterUselessStatistics(dateStatistics);

        ImmutableCollection<Collection<DateStatistic>> dateStatisticsByDay = dateStatistics
                .index(STATISTICS_BY_DATETIME::apply)
                .asMap()
                .values();

        FluentIterable<DateStatistic> summedDateStatistics = FluentIterable
                .from(dateStatisticsByDay)
                .transform(STATISTICS_TO_SUMMEDSTATISTIC::apply);

        List<DateStatistic> filledSummedDateStatistics = addZeroStatisticOnMissingDays(summedDateStatistics.toList());

        List<DateStatistic> accumulatedSumsByDay = accumulateSummedValueOnAllDays(filledSummedDateStatistics);

        return calculateMeanStatistics(accumulatedSumsByDay);
    }

    private FluentIterable<DateStatistic> filterUselessStatistics(FluentIterable<DateStatistic> dateStatistics) {
        Date lastStatisticDate = Collections.max(dateStatistics
                .transform(STATISTICS_BY_DATETIME::apply)
                .toList()).toDate();

        // We do not do calculations with statistics before this date
        final Date firstStatisticDate = DateUtils
                .addMonths(lastStatisticDate, -(MAX_PERIODS_CALCULATION + maxAveragePeriods + 1));

        return dateStatistics
                .filter(dateStatistic -> dateStatistic.getDateTime().isAfter(firstStatisticDate.getTime()));
    }

    private Collection<Statistic> calculateMeanStatistics(List<DateStatistic> accumulatedSumsByDay) {
        ImmutableMap<String, Collection<DateStatistic>> statisticsByPeriod = FluentIterable
                .from(accumulatedSumsByDay)
                .index(DATESTATISTICS_BY_PERIOD::apply)
                .asMap();

        if (statisticsByPeriod.size() == 0) {
            return Lists.newArrayList();
        }

        HashMap<String, Range<DateTime>> periodRanges =
                getPeriodRanges(statisticsByPeriod);
        TreeMap<String, Collection<PeriodRelativeStatistic>> accumulatedRelativeStatisticsByPeriod =
                getPeriodRelativeStatistics(statisticsByPeriod, periodRanges);

        return this.collect(getInterpolatedMeansSuppliersByPeriod(accumulatedRelativeStatisticsByPeriod));
    }

    public List<Statistic> collect(List<Supplier<List<Statistic>>> suppliers) {
        List<Statistic> statistics = Lists.newArrayList();
        for (Supplier<List<Statistic>> supplier : suppliers) {
            try {
                statistics.addAll(supplier.get());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        return statistics;
    }

    public List<Supplier<List<Statistic>>> getInterpolatedMeansSuppliersByPeriod(
            final TreeMap<String, Collection<PeriodRelativeStatistic>> accumulatedRelativeStatisticsByPeriod) {
        List<Supplier<List<Statistic>>> suppliers = Lists.newArrayList();

        final List<String> periods = Lists.newArrayList(accumulatedRelativeStatisticsByPeriod.keySet());

        // How many periods is going to be calculation.
        final int n = Math.min(MAX_PERIODS_CALCULATION, periods.size());
        // Index of the first period (is not 0 if `periods.size()` > MAX_PERIODS_CALCULATION).
        int fistCalculatedPeriod = periods.size() > n ? periods.size() - n : 0;

        for (int i = n - 1; i >= 0; i--) {
            final int indexOfLastPeriod = fistCalculatedPeriod + i;
            final int indexOfFirstPeriod =
                    indexOfLastPeriod > maxAveragePeriods ? indexOfLastPeriod - maxAveragePeriods : 0;

            suppliers.add(() -> {
                // `subMap` returns a view of the portion of map, not copy values.
                SortedMap<String, Collection<PeriodRelativeStatistic>> subMap = accumulatedRelativeStatisticsByPeriod
                        .subMap(periods.get(indexOfFirstPeriod), true, periods.get(indexOfLastPeriod), true);

                return calculateInterpolatedMeans(subMap);
            });
        }

        return suppliers;
    }

    private List<Statistic> calculateInterpolatedMeans(
            SortedMap<String, Collection<PeriodRelativeStatistic>> accumulatedRelativeStatisticsByPeriod) {

        String lastPeriod = accumulatedRelativeStatisticsByPeriod.lastKey();
        Collection<PeriodRelativeStatistic> periodStatistics = accumulatedRelativeStatisticsByPeriod.get(lastPeriod);

        Optional<TreeMap<String, Collection<PeriodRelativeStatistic>>> statisticsForMeanCalculation =
                getStatisticsForMeanCalculation(accumulatedRelativeStatisticsByPeriod);
        if (!statisticsForMeanCalculation.isPresent()) {
            // periodStatistics always have a value
            return fillStatisticForPeriod(lastPeriod,
                    getZeroStatistic(Iterables.getFirst(periodStatistics, null).getStatistic()));
        }

        return calculateInterpolatedMeans(lastPeriod, periodStatistics, statisticsForMeanCalculation.get());
    }

    private List<Statistic> calculateInterpolatedMeans(String period,
            Collection<PeriodRelativeStatistic> periodRelativeStatistics,
            TreeMap<String, Collection<PeriodRelativeStatistic>> statisticsForMeanCalculation) {
        PeriodRelativeMeanInterpolator periodRelativeMeanInterpolator = new PeriodRelativeMeanInterpolator();
        periodRelativeMeanInterpolator.interpolateCompletePeriods(statisticsForMeanCalculation.values());
        periodRelativeStatistics = appendEmptyStatisticsInPeriod(periodRelativeStatistics, period, false);

        List<Statistic> meanStatistics = Lists.newArrayList();
        for (PeriodRelativeStatistic periodRelativeStatistic : periodRelativeStatistics) {
            Statistic statistic = periodRelativeStatistic.getStatistic();
            Statistic meanStatistic = Statistic.copyOf(statistic);

            double meanValue = periodRelativeMeanInterpolator.getMean(
                    periodRelativeStatistic.getPeriodRelativePercentage());
            meanStatistic.setValue(meanValue);
            meanStatistics.add(meanStatistic);
        }
        return meanStatistics;
    }

    private List<Statistic> fillStatisticForPeriod(String period, Statistic sample) {
        DateTime firstPeriod = getTruncatedFirstDateFromPeriod(period);
        DateTime lastPeriod = getTruncatedLastDateFromPeriod(period);
        sample.setPeriod(period);

        return fillStatisticForRange(firstPeriod, lastPeriod, sample);
    }

    private List<Statistic> fillStatisticForRange(DateTime firstDate, DateTime lastDate, Statistic sample) {
        List<Statistic> statistics = Lists.newArrayList();
        int daysBetweenDates = daysBetween(firstDate, lastDate) + 1;
        DateTime current = firstDate;

        for (int i = daysBetweenDates; i > 0; i--) {
            Statistic statistic = Statistic.copyOf(sample);
            statistic.setDescription(ThreadSafeDateFormat.FORMATTER_DAILY.format(current.toDate()));
            statistics.add(statistic);
            current = current.plusDays(1);
        }

        return statistics;
    }

    private Statistic getZeroStatistic(Statistic sample) {
        Statistic zeroStatistic = Statistic.copyOf(sample);
        zeroStatistic.setValue(0);
        return zeroStatistic;
    }

    private Collection<PeriodRelativeStatistic> appendEmptyStatisticsInPeriod(
            Collection<PeriodRelativeStatistic> periodStatistics, String period, boolean firstDateInPeriod) {

        DateTime dateInPeriod;
        if (firstDateInPeriod) {
            dateInPeriod = getTruncatedFirstDateFromPeriod(period);
        } else {
            dateInPeriod = getTruncatedLastDateFromPeriod(period);
        }
        String dateDescription = ThreadSafeDateFormat.FORMATTER_DAILY.format(dateInPeriod.toDate());

        boolean hasFirstDateInPeriodAlready = FluentIterable.from(periodStatistics)
                .anyMatch(periodRelativeWithDescription(dateDescription));

        if (!hasFirstDateInPeriodAlready) {
            List<DateStatistic> dateStatistics = getZeroFilledPeriod(period, periodStatistics, dateInPeriod);

            Range<DateTime> periodRange = getPeriodRange(period);

            List<PeriodRelativeStatistic> relativeComplete = Lists.newArrayList();
            for (DateStatistic dateStatistic : dateStatistics) {
                PeriodRelativeStatistic periodRelativeStatistic = new PeriodRelativeStatistic(periodRange,
                        dateStatistic);
                relativeComplete.add(periodRelativeStatistic);
            }
            return relativeComplete;
        }
        return periodStatistics;
    }

    /**
     * Create fake last date statistics in last period, and fill with empty in between where is missing
     */
    private List<DateStatistic> getZeroFilledPeriod(String period, Collection<PeriodRelativeStatistic> periodStatistics,
            DateTime notExistingDateUsedForPadding) {
        List<DateStatistic> lastPeriodDateStatistics = Lists.newArrayList(FluentIterable
                .from(periodStatistics)
                .transform(PERIODRELATIVESTATISTIC_TO_DATESTATISTIC::apply));

        DateStatistic statisticToCopyFrom = FluentIterable.from(periodStatistics).first().get().getDateStatistic();
        DateStatistic lastStatisticToAddInPeriod = new DateStatistic(statisticToCopyFrom, period,
                notExistingDateUsedForPadding, 0);
        lastPeriodDateStatistics.add(lastStatisticToAddInPeriod);

        return addZeroStatisticOnMissingDays(lastPeriodDateStatistics);
    }

    private static Predicate<PeriodRelativeStatistic> periodRelativeWithDescription(final String lastDateDescription) {
        return periodRelativeStatistic -> Objects
                .equal(periodRelativeStatistic.getStatistic().getDescription(), lastDateDescription);
    }

    private Optional<TreeMap<String, Collection<PeriodRelativeStatistic>>> getStatisticsForMeanCalculation(
            SortedMap<String, Collection<PeriodRelativeStatistic>> accumulatedRelativeStatisticsByPeriod) {

        TreeMap<String, Collection<PeriodRelativeStatistic>> validMeanPeriods =
                new TreeMap<>(accumulatedRelativeStatisticsByPeriod);

        String lastKey = accumulatedRelativeStatisticsByPeriod.lastKey();

        validMeanPeriods.remove(lastKey);

        if (validMeanPeriods.size() == 0) {
            return Optional.empty();
        }

        String firstKey = accumulatedRelativeStatisticsByPeriod.firstKey();

        boolean hasCompleteFirstPeriod = FluentIterable
                .from(accumulatedRelativeStatisticsByPeriod.get(firstKey))
                .anyMatch(PERIODRELATIVE_PERCENTAGE_IS0);

        if (!hasCompleteFirstPeriod) {
            validMeanPeriods.remove(firstKey);

            if (validMeanPeriods.size() == 0) {
                return Optional.empty();
            }
        }

        TreeMap<String, Collection<PeriodRelativeStatistic>> meanCalculationPeriods = Maps.newTreeMap();

        // Put the guaranteed entry to exist (since above cleanup logic)
        Map.Entry<String, Collection<PeriodRelativeStatistic>> previousPeriod = validMeanPeriods.lastEntry();
        meanCalculationPeriods.put(previousPeriod.getKey(), previousPeriod.getValue());
        int periodsLeft = validMeanPeriods.size() - 1;

        // ...and go backwards from the last entry until the number of periods that we use for calculation or we have no stats
        while (periodsLeft > 0) {
            Map.Entry<String, Collection<PeriodRelativeStatistic>> currentPeriod = validMeanPeriods
                    .lowerEntry(previousPeriod.getKey());
            meanCalculationPeriods.put(currentPeriod.getKey(), currentPeriod.getValue());

            periodsLeft--;
            previousPeriod = currentPeriod;
        }

        return Optional.of(meanCalculationPeriods);
    }

    private HashMap<String, Range<DateTime>> getPeriodRanges(
            ImmutableMap<String, Collection<DateStatistic>> statisticsByPeriod) {
        HashMap<String, Range<DateTime>> periodRanges = Maps.newHashMap();

        for (String period : statisticsByPeriod.keySet()) {
            Range<DateTime> periodRange = getPeriodRange(period);
            periodRanges.put(period, periodRange);
        }

        return periodRanges;
    }

    private Range<DateTime> getPeriodRange(String period) {
        return Range.closed(getTruncatedFirstDateFromPeriod(period), getTruncatedLastDateFromPeriod(period));
    }

    private DateTime getTruncatedFirstDateFromPeriod(String period) {
        Date firstDateFromPeriod = DateUtils
                .getFirstDateFromPeriod(period, resolution, user.getProfile().getPeriodAdjustedDay());
        firstDateFromPeriod = DateUtils.setInclusiveStartTime(firstDateFromPeriod);
        return new DateTime(firstDateFromPeriod).withZoneRetainFields(DateTimeZone.UTC);
    }

    private DateTime getTruncatedLastDateFromPeriod(String period) {
        Date lastDateFromPeriod = DateUtils
                .getLastDateFromPeriod(period, resolution, user.getProfile().getPeriodAdjustedDay());
        lastDateFromPeriod = DateUtils.setInclusiveStartTime(lastDateFromPeriod);
        return new DateTime(lastDateFromPeriod).withZoneRetainFields(DateTimeZone.UTC);
    }

    private TreeMap<String, Collection<PeriodRelativeStatistic>> getPeriodRelativeStatistics(
            ImmutableMap<String, Collection<DateStatistic>> statisticsByPeriod,
            HashMap<String, Range<DateTime>> periodRanges) {
        TreeMap<String, Collection<PeriodRelativeStatistic>> relativeStatisticsByPeriod = Maps.newTreeMap();

        for (String period : statisticsByPeriod.keySet()) {
            Range<DateTime> periodRange = periodRanges.get(period);
            List<PeriodRelativeStatistic> periodRelativeStatistics = Lists.newArrayList();

            for (DateStatistic dateStatistic : statisticsByPeriod.get(period)) {
                periodRelativeStatistics.add(new PeriodRelativeStatistic(periodRange, dateStatistic));
            }

            relativeStatisticsByPeriod.put(period, periodRelativeStatistics);
        }

        return relativeStatisticsByPeriod;
    }

    private List<DateStatistic> addZeroStatisticOnMissingDays(List<DateStatistic> dateStatistics) {
        if (dateStatistics.size() < 2) {
            return dateStatistics;
        }

        List<DateStatistic> completeStatistics = Lists.newArrayList();

        ImmutableList<DateStatistic> sortedDateStatistics = FluentIterable
                .from(dateStatistics)
                .toSortedList(ORDERED_BY_DATE);

        DateStatistic currentStatistic = sortedDateStatistics.get(0);
        completeStatistics.add(currentStatistic);

        for (int i = 1; i < sortedDateStatistics.size(); i++) {
            DateStatistic nextStatistic = sortedDateStatistics.get(i);
            int daysBetween = daysBetween(currentStatistic.getDateTime(), nextStatistic.getDateTime());

            while (daysBetween > 1) {
                currentStatistic = createEmptyStatisticAfter(currentStatistic);
                completeStatistics.add(currentStatistic);
                daysBetween--;
            }

            currentStatistic = nextStatistic;
            completeStatistics.add(currentStatistic);
        }

        return completeStatistics;
    }

    int daysBetween(DateTime firstDate, DateTime nextDate) {
        return Days.daysBetween(firstDate.toLocalDate(), nextDate.toLocalDate()).getDays();
    }

    private DateStatistic createEmptyStatisticAfter(DateStatistic statistic) {
        DateStatistic dateStatistic = new DateStatistic(statistic, 1);

        Statistic emptyStatistic = dateStatistic.getStatistic();
        emptyStatistic.setValue(0);
        emptyStatistic.setPeriod(
                DateUtils.getMonthPeriod(dateStatistic.getDateTime().toDate(), resolution,
                        user.getProfile().getPeriodAdjustedDay())
        );

        return dateStatistic;
    }

    private List<DateStatistic> accumulateSummedValueOnAllDays(List<DateStatistic> filledDateStatisticsSummedByDay) {
        List<DateStatistic> accumulatedStatistics = Lists.newArrayList();

        // Sort by day to make sure we accumulate asc in months
        ImmutableList<DateStatistic> sortedSumsByDay = FluentIterable
                .from(filledDateStatisticsSummedByDay)
                .toSortedList(ORDERED_BY_DATE);

        // Accumulate over all days, and accumulate sums in each period
        String currentPeriod = sortedSumsByDay.get(0).getStatistic().getPeriod();
        double accumulatedSum = 0;
        for (DateStatistic dateStatistic : sortedSumsByDay) {
            String period = dateStatistic.getStatistic().getPeriod();
            if (!Objects.equal(period, currentPeriod)) {
                accumulatedSum = 0;
                currentPeriod = period;
            }

            accumulatedSum += dateStatistic.getStatistic().getValue();

            DateStatistic accumulatedStatistic = new DateStatistic(dateStatistic);
            accumulatedStatistic.getStatistic().setValue(accumulatedSum);
            accumulatedStatistics.add(accumulatedStatistic);
        }

        return accumulatedStatistics;
    }

    private static final Function<Statistic, DateStatistic> STATISTIC_TO_DATESTATISTIC =
            new Function<Statistic, DateStatistic>() {
                @Override
                @Nullable
                public DateStatistic apply(Statistic statistic) {
                    try {
                        return new DateStatistic(statistic);
                    } catch (ParseException e) {
                        return null;
                    }
                }
            };

    private static final Function<DateStatistic, DateTime> STATISTICS_BY_DATETIME =
            DateStatistic::getDateTime;

    private static final Function<Collection<DateStatistic>, DateStatistic> STATISTICS_TO_SUMMEDSTATISTIC =
            dateStatistics -> {
                double sum = 0.00;

                for (DateStatistic statistic : dateStatistics) {
                    sum += statistic.getStatistic().getValue();
                }

                @SuppressWarnings("OptionalGetWithoutIsPresent")
                DateStatistic summedStatistic = new DateStatistic(
                        FluentIterable.from(dateStatistics).first().get()
                );
                summedStatistic.getStatistic().setValue(sum);

                return summedStatistic;
            };

    private static final Comparator<? super DateStatistic> ORDERED_BY_DATE =
            (Comparator<DateStatistic>) (left, right) -> left.getDateTime().compareTo(right.getDateTime());

    private static final Function<DateStatistic, String> DATESTATISTICS_BY_PERIOD =
            dateStatistic -> dateStatistic.getStatistic().getPeriod();

    private static final Predicate<PeriodRelativeStatistic> PERIODRELATIVE_PERCENTAGE_IS0 =
            periodRelativeStatistic -> periodRelativeStatistic.getPeriodRelativePercentage() == 0;

    private static final Predicate<PeriodRelativeStatistic> PERIODRELATIVE_PERCENTAGE_IS1 =
            periodRelativeStatistic -> periodRelativeStatistic.getPeriodRelativePercentage() == 1;

    private static final Function<PeriodRelativeStatistic, DateStatistic> PERIODRELATIVESTATISTIC_TO_DATESTATISTIC =
            PeriodRelativeStatistic::getDateStatistic;
}
