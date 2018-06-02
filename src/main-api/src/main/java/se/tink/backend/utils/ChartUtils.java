package se.tink.backend.utils;

import com.google.common.collect.Lists;
import com.google.common.math.DoubleMath;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import se.tink.backend.core.Balance;
import se.tink.backend.core.LeftToSpendBalance;
import se.tink.backend.core.Statistic;
import se.tink.libraries.date.DateUtils;

public class ChartUtils {
    
    public static class BalanceCharts {
        public static List<DateTime> getDatesFromBalances(List<Balance> balances) {
            return Lists.transform(balances, Balance::getDate);
        }

        public static double getMax(List<Balance> balances) {
            if (balances == null || balances.isEmpty()) {
                return 0;
            }
            
            return Collections.max(balances, Comparators.BALANCE_BY_AMOUNT).getAmount();
        }

        public static double getMax(List<Balance> balances1, List<Balance> balances2) {
            return Math.max(getMax(balances1), getMax(balances2));
        }

        public static double getMin(List<Balance> balances) {
            if (balances == null || balances.isEmpty()) {
                return 0;
            }

            return Collections.min(balances, Comparators.BALANCE_BY_AMOUNT).getAmount();
        }

        public static double getMin(List<Balance> balances1, List<Balance> balances2) {
            return Math.min(getMin(balances1), getMin(balances2));
        }
    }

    public static class LeftToSpendCharts {
        public static List<LeftToSpendBalance> getBalancesFromStatistics(List<Statistic> statistics) {
            return Lists.transform(statistics, s -> {
                DateTime dateTime = DateUtils.convertDate(s.getDescription());
                return new LeftToSpendBalance(dateTime, s.getValue(), s.getPayload());
            });
        }

        public static double getMax(List<LeftToSpendBalance> balances1, List<LeftToSpendBalance> balances2) {
            return BalanceCharts.getMax(new ArrayList<Balance>(balances1), new ArrayList<Balance>(balances2));
        }

        public static double getMin(List<LeftToSpendBalance> balances1, List<LeftToSpendBalance> balances2) {
            return BalanceCharts.getMin(new ArrayList<Balance>(balances1), new ArrayList<Balance>(balances2));
        }
    }

    public enum PeriodType {
        Day,
        Month,
        Quarter,
        ThreeDays,
        TwoWeeks,
        Week
    }

    public static DateTime getFirstLabelDate(DateTime date, PeriodType periodType, Calendar calendar) {
        if (isValidLabelDate(date, periodType, calendar)) {
            return date;
        }
        
        switch (periodType) {
        case Quarter:
            return DateUtils.nextNewQuarterDate(date);
        case Month:
            return DateUtils.nextNewMonthDate(date);
        case TwoWeeks:
            return DateUtils.nextFirstDayOfWeek(date, calendar);
        case Week:
            return DateUtils.nextFirstDayOfWeek(date, calendar);
        case ThreeDays:
        case Day:
        default:
            return date;
        }
    }

    public static List<Double> getGuidelines(double minValue, double maxValue, int maxLines) {
        return getGuidelines(minValue, maxValue, maxLines, false);
    }

    public static List<Double> getGuidelines(double minValue, double maxValue, int maxLines, boolean includeZeroLine) {

        List<Double> guidelines = Lists.newArrayList();
        
        if (maxLines < 1 || DoubleMath.fuzzyEquals(minValue, maxValue, 0.0001) || minValue > maxValue) {
            return guidelines;
        }

        double range = maxValue - minValue;
        int digits = numDigits((int) range);
        double resolution = getResolutionForGuidelines(digits);
        double maxLinesNeeded = range / resolution;
        double linesFactor = maxLinesNeeded / maxLines;
        double step = ceiling(resolution * linesFactor, resolution);
        
        // Unable to generate guidelines if the step is 0.
        if (DoubleMath.fuzzyEquals(step, 0, 0.0001)) {
            return guidelines;
        }

        for (double value = floor(maxValue, step); value >= minValue && guidelines.size() < maxLines; value -= step) {
            // No need to include the zero line---it's already covered by the x axis.
            if (!includeZeroLine && DoubleMath.fuzzyEquals(value, 0, 0.0001)) {
                continue;
            }

            guidelines.add(Math.round(value * 10) / 10d);
        }

        return guidelines;
    }

    public static double getResolutionForGuidelines(int digits) {
        if (digits == 2) {
            return 5;
        } else {
            return Math.pow(10, digits - 2);
        }
    }

    public static PeriodType getStepPeriodType(DateTime firstDate, DateTime lastDate, Calendar calendar) {

        DateTime firstDrawableDate = DateUtils.nextNewQuarterDate(firstDate);
        if (DateUtils.isMoreThanMonthsBefore(firstDrawableDate, lastDate, 6)) {
            return PeriodType.Quarter;
        }
        
        firstDrawableDate = DateUtils.nextNewMonthDate(firstDate);
        if (DateUtils.isMoreThanMonthsBefore(firstDrawableDate, lastDate, 2)) {
            return PeriodType.Month;
        } else if (firstDate.getDayOfWeek() == calendar.getFirstDayOfWeek()) {
            firstDrawableDate = firstDate;
        } else {
            firstDrawableDate = DateUtils.nextFirstDayOfWeek(firstDate, calendar);
        }
        
        if (DateUtils.isMoreThanWeeksApart(firstDrawableDate, lastDate, 4)) {
            return PeriodType.TwoWeeks;
        } else if (DateUtils.isMoreThanWeeksApart(firstDrawableDate, lastDate, 2)) {
            return PeriodType.Week;
        }
        
        firstDrawableDate = firstDate;
        if (DateUtils.isMoreThanDaysApart(firstDrawableDate, lastDate, 6)) {
            return PeriodType.ThreeDays;
        } else {
            return PeriodType.Day;
        }
    }

    public static boolean isValidLabelDate(DateTime date, PeriodType periodType, Calendar calendar) {
        switch (periodType) {
        case Quarter:
            return date.getDayOfMonth() == 1 && date.getMonthOfYear() % 3 == 0;
        case Month:
            return date.getDayOfMonth() == 1;
        case TwoWeeks:
        case Week:
            return date.getDayOfWeek() == calendar.getFirstDayOfWeek();
        case ThreeDays:
        case Day:
            return true;
        default:
            return false;
        }
    }

    public static DateTime nextLabelDate(DateTime date, PeriodType periodType, Calendar calendar) {
        switch (periodType) {
        case Quarter:
            return DateUtils.nextNewQuarterDate(date);
        case Month:
            return DateUtils.nextNewMonthDate(date);
        case TwoWeeks:
            return DateUtils.nextFirstDayOfWeek(DateUtils.nextFirstDayOfWeek(date, calendar), calendar);
        case Week:
            return DateUtils.nextFirstDayOfWeek(date, calendar);
        case ThreeDays:
            return date.plusDays(3);
        case Day:
            return date.plusDays(1);
        default:
            return date;
        }
    }

    public static int numDigits(int n) {
        if (n == 0) {
            return 1;
        } else {
            return ((int)Math.log10(Math.abs(n))) + 1;
        }
    }

    /**
     * Round _up_ `n` to closest value divisible by `resolution`.
     * @param n
     * @param resolution
     * @return
     */
    public static double ceiling(double n, double resolution) {
        if (DoubleMath.fuzzyEquals(0, resolution, 0.0001)) {
            return n;
        } else {
            return Math.ceil(n / resolution) * resolution;
        }
    }

    /**
     * Round _down_ `n` to closest value divisible by `resolution`.
     * @param n
     * @param resolution
     * @return
     */
    public static double floor(double n, double resolution) {
        if (DoubleMath.fuzzyEquals(0, resolution, 0.0001)) {
            return n;
        } else {
            return Math.floor(n / resolution) * resolution;
        }
    }
    
    /**
     * Round `n` to closest value divisible by `resolution`.
     * @param n
     * @param resolution
     * @return
     */
    public static double round(double n, double resolution) {
        if (DoubleMath.fuzzyEquals(0, resolution, 0.0001)) {
            return n;
        } else {
            return Math.round(n / resolution) * resolution;
        }
    }
}
