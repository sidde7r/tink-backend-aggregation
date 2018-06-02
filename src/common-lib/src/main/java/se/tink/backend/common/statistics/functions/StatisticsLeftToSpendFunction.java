package se.tink.backend.common.statistics.functions;

import java.text.ParseException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import java.util.function.Function;
import org.apache.commons.lang.time.DateUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;

import se.tink.libraries.date.ResolutionTypes;
import se.tink.backend.core.Statistic;
import se.tink.backend.core.User;
import se.tink.backend.utils.LogUtils;
import se.tink.libraries.date.ThreadSafeDateFormat;

import com.google.common.collect.ComparisonChain;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;

/**
 * Reducer that sums value but set month period
 */
public class StatisticsLeftToSpendFunction implements Function<Collection<Statistic>, Collection<Statistic>> {
    private static final LogUtils log = new LogUtils(StatisticsLeftToSpendFunction.class);

    private User user;
    private ResolutionTypes resolution;

    protected static final Ordering<Statistic> STATISTICS_ORDERING = new Ordering<Statistic>() {
        public int compare(Statistic left, Statistic right) {
            return ComparisonChain.start().compare(left.getDescription(), right.getDescription()).result();
        }
    };

    public StatisticsLeftToSpendFunction(ResolutionTypes resolution, User user) {
        this.resolution = resolution;
        this.user = user;
    }

    public Collection<Statistic> apply(Collection<Statistic> ss) {
        return fillEmptyDaysWithStatistics(ss, false);
    }

    public Collection<Statistic> fillEmptyDaysWithStatistics(Collection<Statistic> ss, boolean isAverageCalculation) {
        List<Statistic> resultStatistics = Lists.newArrayList();
        Map<String, Statistic> perDayStatistics = Maps.newHashMap();
        Statistic lastStatistic = null;

        // sum per day

        for (Statistic s : ss) {
            if (perDayStatistics.containsKey(s.getDescription())) {
                perDayStatistics.get(s.getDescription()).setValue(
                        perDayStatistics.get(s.getDescription()).getValue() + s.getValue());
            } else {
                perDayStatistics.put(s.getDescription(), s);
            }
        }

        List<Statistic> sortedStatistics = STATISTICS_ORDERING.sortedCopy(perDayStatistics.values());
        DateTime checkDate = null;
        int daysBetween;

        try {
            for (Statistic s : sortedStatistics) {

                if (lastStatistic == null) {

                    // this is the first date of the period for the first statistics

                    checkDate = new DateTime(se.tink.libraries.date.DateUtils.getFirstDateFromPeriod(s.getPeriod(),
                            resolution, user.getProfile().getPeriodAdjustedDay()));

                    // this is the first data of all statistics

                    DateTime firstStatDate = new DateTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(s.getDescription()));

                    daysBetween = Days.daysBetween(checkDate, firstStatDate).getDays();

                    // just check so we don't end up in a loop

                    if (isInvalidDaysBetween(daysBetween, s)) {
                        break;
                    }

                    // add statistics before the first statistic
                    for (int i = 0; i < daysBetween; i++) {
                        lastStatistic = Statistic.copyOf(s);
                        lastStatistic.setValue(0);
                        Date addDate = DateUtils.addDays(checkDate.toDate(), i);
                        lastStatistic.setDescription(ThreadSafeDateFormat.FORMATTER_DAILY.format(addDate));
                        resultStatistics.add(lastStatistic);
                    }
                    lastStatistic = Statistic.copyOf(s);
                    resultStatistics.add(s);
                    continue;
                }

                // pad statistic within periods

                checkDate = new DateTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(lastStatistic.getDescription()));
                DateTime thisStat = new DateTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(s.getDescription()));
                daysBetween = Days.daysBetween(checkDate, thisStat).getDays();

                if (isInvalidDaysBetween(daysBetween, s)) {
                    break;
                }

                for (int i = 1; i < daysBetween; i++) {
                    Statistic copyStat = Statistic.copyOf(lastStatistic);
                    copyStat.setDescription(ThreadSafeDateFormat.FORMATTER_DAILY.format(DateUtils.addDays(checkDate.toDate(), i)));
                    resultStatistics.add(copyStat);
                }

                // add last value to this period

                s.setValue(s.getValue() + lastStatistic.getValue());
                resultStatistics.add(s);
                lastStatistic = Statistic.copyOf(s);
            }

            // add missing statistics at end of series

            Statistic lastInSerie = Iterables.getLast(resultStatistics);

            DateTime today = new DateTime();
            DateTime lastKnownDate = new DateTime(ThreadSafeDateFormat.FORMATTER_DAILY.parse(lastInSerie.getDescription()));
            DateTime lastDateInPeriod = new DateTime(se.tink.libraries.date.DateUtils.getLastDateFromPeriod(
                    lastInSerie.getPeriod(), resolution, user.getProfile().getPeriodAdjustedDay()));
            String lastKnownPeriod = lastInSerie.getPeriod();
            String currentPeriod = se.tink.libraries.date.DateUtils.getCurrentMonthPeriod(resolution,
                    user.getProfile().getPeriodAdjustedDay());
            double value = lastInSerie.getValue();

            // in current period, for average statistics calculation, fill with 0 to know the period range
            if (lastKnownPeriod.equals(currentPeriod) && isAverageCalculation) {
                daysBetween = Days.daysBetween(lastKnownDate, lastDateInPeriod).getDays();
                value = 0;
            }

            // not in current period, fill to end of period
            else if (!lastKnownPeriod.equals(currentPeriod)) {
                daysBetween = Days.daysBetween(lastKnownDate, lastDateInPeriod).getDays();
            }// in current period, fill to today
            else {
                daysBetween = Days.daysBetween(lastKnownDate, today).getDays();
            }

            if (isInvalidDaysBetween(daysBetween, lastInSerie)) {
                return resultStatistics;
            }

            for (int i = 1; i <= daysBetween; i++) {
                Statistic stat = Statistic.copyOf(lastInSerie);
                stat.setValue(value);
                Date addDate = DateUtils.addDays(lastKnownDate.toDate(), i);
                stat.setDescription(ThreadSafeDateFormat.FORMATTER_DAILY.format(addDate));
                resultStatistics.add(resultStatistics.size(), stat);
            }

        } catch (ParseException e) {
            log.error("Could not sum per day and pad empty days statistics", e);
        }

        return resultStatistics;
    }

    private boolean isInvalidDaysBetween(int daysBetween, Statistic s) {
        // validate we don't get stuck in an infinite loop, a period cannot be more days than two months
        if (daysBetween < 0 || daysBetween > 60) {
            log.error(
                    s.getUserId(),
                    "Something went wrong when adding empty statistics befor first transaction in period "
                            + s.getPeriod());
            return true;
        }
        return false;
    }
}
