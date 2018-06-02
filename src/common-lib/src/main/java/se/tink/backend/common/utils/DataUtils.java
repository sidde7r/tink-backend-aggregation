package se.tink.backend.common.utils;

import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.ComparisonChain;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableListMultimap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Multimaps;
import com.google.common.collect.Ordering;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.annotation.Nullable;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.StringDoublePair;
import se.tink.libraries.date.DateUtils;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

public class DataUtils {
    private static final LogUtils log = new LogUtils(DataUtils.class);
    
    private static final Ordering<StringDoublePair> STRING_DOUBLE_KEY_ORDERING = new Ordering<StringDoublePair>() {
        public int compare(StringDoublePair left, StringDoublePair right) {
            return ComparisonChain.start().compare(left.getKey(), right.getKey()).result();
        }
    };

    private static final Predicate<Statistic> TEMP_FIX_REMOVE_2016_53_STATISTICS_PREDICATE = s -> !Objects
            .equals(s.getPeriod(), "2016:53");
    
    private static final Ordering<String> STRING_ORDERING = Ordering.natural();

    /**
     * Helper method to aggregate (sum) a list of statistics based on the period into a list of KVPairs.
     * 
     * @param statistics
     * @return
     */
    public static List<StringDoublePair> aggregateStatisticsToKVPairs(Iterable<Statistic> statistics) {
        ImmutableListMultimap<String, Statistic> statisticsByKey = Multimaps.index(statistics,
                Statistic::getPeriod);

        ImmutableSet<String> keys = statisticsByKey.keySet();
        List<StringDoublePair> aggregatedStatistics = Lists.newArrayListWithCapacity(keys.size());

        for (String key : keys) {
            double value = 0;

            for (Statistic s : statisticsByKey.get(key)) {
                value += s.getValue();
            }

            aggregatedStatistics.add(new StringDoublePair(key, value));
        }

        return STRING_DOUBLE_KEY_ORDERING.sortedCopy(aggregatedStatistics);
    }

    public static List<StringDoublePair> transformStatisticsToKVPairs(Iterable<Statistic> statistics) {
        return STRING_DOUBLE_KEY_ORDERING.sortedCopy(Iterables.transform(statistics,
                new Function<Statistic, StringDoublePair>() {
                    @Nullable
                    @Override
                    public StringDoublePair apply(@Nullable Statistic statistic) {
                        return statistic == null ? null : new StringDoublePair(statistic.getPeriod(), statistic.getValue());
                    }
                }));
    }

    /**
     * Helper method to calculate average of a List of StringDoublePair.
     * 
     * @param list
     * @return the average amount
     */
    static double calculateAverageAmount(List<StringDoublePair> list) {
        if (list == null || list.size() == 0)
            return 0;

        double sum = 0d;
        for (StringDoublePair pair : list) {
            sum += pair.getValue();
        }

        return sum / list.size();
    }

    /**
     * Helper method to make a cumulative sum of data-points.
     * 
     * @param input
     * @return
     */
    static List<StringDoublePair> cumSum(List<StringDoublePair> input) {
        double cumsum = 0;

        List<StringDoublePair> output = Lists.newArrayList();

        for (StringDoublePair d : input) {
            cumsum += d.getValue();

            output.add(new StringDoublePair(d.getKey(), cumsum));
        }

        return output;
    }

    /**
     * Limit a time series based on a reverse basis.
     * 
     * @param input
     * @param count
     * @return
     */
    public static List<StringDoublePair> limit(List<StringDoublePair> input, int count) {
        return Lists.reverse(Lists.newArrayList(Iterables.limit(
                Lists.reverse(STRING_DOUBLE_KEY_ORDERING.sortedCopy(input)), count)));
    }
    
    /**
     * Helper method to pad a time series key values. 
     * That is, add values = 0 for keys.
     * 
     * @param input
     * @param keys
     * @return
     */
    public static List<StringDoublePair> pad(List<StringDoublePair> input, Set<String> keys) {
        Map<String, StringDoublePair> inputByKey = Maps.newHashMap(Maps.uniqueIndex(input,
                d -> (d.getKey())));

        for (String key : keys) {
            if (!inputByKey.containsKey(key)) {
                inputByKey.put(key, new StringDoublePair(key, 0d));
            }
        }

        return STRING_DOUBLE_KEY_ORDERING.sortedCopy(inputByKey.values());
    }
    
    /**
     * Helper method to zero-fill KVPair data-points. Starts from first (latest) key and put
     * 0 at gaps to end of series.  
     * 
     * @param dataPoints
     * @param resolution
     * @return
     */
    @SuppressWarnings("MagicConstant")
    public static List<StringDoublePair> zeroFill(List<StringDoublePair> dataPoints, ResolutionTypes resolution) {
        if (dataPoints.isEmpty()) {
            return Lists.newArrayList();
        }

        try {
            Map<String, StringDoublePair> dataPointsByKey = Maps.uniqueIndex(dataPoints,
                    StringDoublePair::getKey);

            String firstKey = STRING_ORDERING.min(dataPointsByKey.keySet());
            String lastKey = STRING_ORDERING.max(dataPointsByKey.keySet());

            ThreadSafeDateFormat dateFormat = getDateFormatForResolution(resolution);
            int fieldToIncrement = getDateIncrementFieldForResolution(resolution);

            Calendar date = DateUtils.getCalendar();
            date.setTime(DateUtils.flattenTime(dateFormat.parse(firstKey)));

            Date lastDate = DateUtils.flattenTime(dateFormat.parse(lastKey));

            List<StringDoublePair> output = Lists.newArrayList();

            while (!date.getTime().after(lastDate)) {
                String key = dateFormat.format(date.getTime());

                StringDoublePair dataPoint = dataPointsByKey.get(key);

                if (dataPoint == null) {
                    dataPoint = new StringDoublePair(key, 0d);
                }

                output.add(dataPoint);

                date.add(fieldToIncrement, 1);
            }

            return output;
        } catch (ParseException e) {
            log.error("Could not zero-fill data points", e);
            return dataPoints;
        }
    }
    
    /**
     * Helper method to take the previous value from the KVPair data-points and fill the empty gap. 
     * Starts from first (latest) period.
     *  
     * @param dataPoints
     * @param resolution
     * @return
     */
    @SuppressWarnings("MagicConstant")
    public static List<Statistic> flatFill(
            List<Statistic> dataPoints, ResolutionTypes resolution, boolean fillUntilToday) {
        if (dataPoints.isEmpty()) {
            return Lists.newArrayList();
        }

        try {

            Map<String, Statistic> dataPointsByKey = FluentIterable.from(dataPoints)
                    .filter(TEMP_FIX_REMOVE_2016_53_STATISTICS_PREDICATE)
                    .uniqueIndex(Statistic::getPeriod);

            if (dataPointsByKey.isEmpty()) {
                return Lists.newArrayList();
            }

            String firstKey = STRING_ORDERING.min(dataPointsByKey.keySet());

            ThreadSafeDateFormat dateFormat = getDateFormatForResolution(resolution);
            int fieldToIncrement = getDateIncrementFieldForResolution(resolution);

            Calendar date = DateUtils.getCalendar();
            date.setTime(DateUtils.flattenTime(dateFormat.parse(firstKey)));

            Date flatFillTo;
            if (fillUntilToday) {
                flatFillTo = DateUtils.getToday();
            } else {
                String lastKey = STRING_ORDERING.max(dataPointsByKey.keySet());
                flatFillTo = DateUtils.flattenTime(dateFormat.parse(lastKey));
            }

            List<Statistic> output = Lists.newArrayList();

            while (!date.getTime().after(flatFillTo)) {
                String key = dateFormat.format(date.getTime());

                Statistic dataPoint = dataPointsByKey.get(key);

                if (dataPoint == null) {
                    dataPoint = Statistic.copyOf(Iterables.getLast(output));
                    dataPoint.setPeriod(key);
                }

                output.add(dataPoint);

                date.add(fieldToIncrement, 1);
            }

            return output;
        } catch (ParseException e) {
            log.error("Could not zero-fill data points", e);
            return dataPoints;
        }
    }

    /**
     * Helper method to take a data point and copy/fill that until we get up to (and equal) today.
     */
    @SuppressWarnings("MagicConstant")
    public static List<Statistic> flatFillUntilToday(Statistic dataPoint, ResolutionTypes resolution) {
        if (dataPoint == null) {
            return Lists.newArrayList();
        }

        if (Objects.equals(dataPoint.getPeriod(), "2016:53")) {
            // Very temporary solution for faulty data
            return Lists.newArrayList();
        }

        List<Statistic> output = Lists.newArrayList();

        try {

            ThreadSafeDateFormat dateFormat = getDateFormatForResolution(resolution);
            int fieldToIncrement = getDateIncrementFieldForResolution(resolution);

            Calendar date = DateUtils.getCalendar();
            date.setTime(DateUtils.flattenTime(dateFormat.parse(dataPoint.getPeriod())));

            date.add(fieldToIncrement, 1);

            Date today = DateUtils.getToday();

            while (date.getTime().getTime() <= today.getTime()) {
                String key = dateFormat.format(date.getTime());

                Statistic copy = Statistic.copyOf(dataPoint);
                copy.setPeriod(key);
                output.add(copy);

                date.add(fieldToIncrement, 1);
            }

            return output;
        } catch (ParseException e) {
            log.error("Could not flat-fill data points", e);
            return output;
        }
    }

    private static ThreadSafeDateFormat getDateFormatForResolution(ResolutionTypes resolution) {
        switch (resolution) {
        case YEARLY:
            return ThreadSafeDateFormat.FORMATTER_YEARLY;
        case MONTHLY:
        case MONTHLY_ADJUSTED:
            return ThreadSafeDateFormat.FORMATTER_MONTHLY;
        case DAILY:
            return ThreadSafeDateFormat.FORMATTER_DAILY;
        case WEEKLY:
            return ThreadSafeDateFormat.FORMATTER_WEEKLY;
        default:
            throw new RuntimeException("unsupported resolution: " + resolution);
        }
    }

    private static int getDateIncrementFieldForResolution(ResolutionTypes resolution) {
        switch (resolution) {
        case YEARLY:
            return Calendar.YEAR;
        case MONTHLY:
        case MONTHLY_ADJUSTED:
            return Calendar.MONTH;
        case DAILY:
            return Calendar.DAY_OF_YEAR;
        case WEEKLY:
            return Calendar.WEEK_OF_YEAR;
        default:
            throw new RuntimeException("unsupported resolution: " + resolution);
        }
    }

}
