package se.tink.libraries.date;

import com.google.common.collect.ImmutableMap;
import java.util.Calendar;
import java.util.Date;

/**
 * Helper functions for getting the first and last business days for a period, depending on the
 * user's break date.
 */
public class FirstAndLastBusinessDays {

    // Last/first business days in a period for all possible break dates (1-31).
    private static final ImmutableMap<String, ImmutableMap<Integer, Date>> LAST_DAY_OF_PERIODS;
    private static final ImmutableMap<String, ImmutableMap<Integer, Date>> FIRST_DAY_OF_PERIODS;
    private static final int START_YEAR = 1999;
    private static final int END_YEAR = Calendar.getInstance().get(Calendar.YEAR) + 1;

    static {

        // With START_YEAR = 1999 and END_YEAR = 2017, the following retained size was observed from
        // a heap dump:
        // class se.tink.libraries.date.FirstAndLastBusinessDays 1 272 632 bytes
        FIRST_DAY_OF_PERIODS = createBusinessDaysBuilder(true, START_YEAR, END_YEAR).build();
        LAST_DAY_OF_PERIODS = createBusinessDaysBuilder(false, START_YEAR, END_YEAR).build();
    }

    /**
     * Helper method for creating a builder for an immutable business days map and setting
     * first/last business days for every period. This is set for every possible break date (1-31)
     * and all of this between a chosen period of time.
     *
     * @param first True if first date in period should be calculated, false if last date.
     * @param startYear The year to start the first/last business day calculations.
     * @param endYear The year to end the first/last business day calculations.
     */
    private static ImmutableMap.Builder<String, ImmutableMap<Integer, Date>>
            createBusinessDaysBuilder(boolean first, int startYear, int endYear) {

        // Create builder and set the given start date.
        ImmutableMap.Builder<String, ImmutableMap<Integer, Date>> businessDaysBuilder =
                ImmutableMap.builder();
        Calendar calendarPeriod = DateUtils.getCalendar();
        calendarPeriod.set(startYear, Calendar.JANUARY, 1);

        // Create entries for every period and every possible break date (1-31) until the given
        // year.
        while (calendarPeriod.get(Calendar.YEAR) <= endYear) {
            ImmutableMap.Builder<Integer, Date> dateForBreakDatesBuilder = ImmutableMap.builder();
            String period = ThreadSafeDateFormat.FORMATTER_MONTHLY.format(calendarPeriod.getTime());
            for (Integer breakDay = 1; breakDay <= 31; breakDay++) {
                Date date =
                        (first
                                ? getFirstDateHelper(calendarPeriod, breakDay)
                                : getLastDateHelper(calendarPeriod, breakDay));
                dateForBreakDatesBuilder.put(breakDay, date);
            }
            businessDaysBuilder.put(period, dateForBreakDatesBuilder.build());
            calendarPeriod.add(Calendar.MONTH, 1);
        }
        return businessDaysBuilder;
    }

    /**
     * Helper method for getting first date in a period depending on monthly adjusted period break
     * date.
     */
    private static Date getFirstDateHelper(Calendar calendarPeriod, int periodBreakDate) {
        Calendar calendar = (Calendar) calendarPeriod.clone();
        calendar.add(Calendar.MONTH, -1);
        calendar.set(
                Calendar.DATE, Math.min(periodBreakDate, calendar.getActualMaximum(Calendar.DATE)));

        while (!DateUtils.isBusinessDay(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        DateUtils.setInclusiveStartTime(calendar);
        return calendar.getTime();
    }

    /**
     * Helper method for getting last date in a period depending on monthly adjusted period break
     * date.
     */
    private static Date getLastDateHelper(Calendar calendarPeriod, int breakDate) {
        Calendar calendar = (Calendar) calendarPeriod.clone();
        calendar.set(Calendar.DATE, Math.min(breakDate, calendar.getActualMaximum(Calendar.DATE)));

        while (!DateUtils.isBusinessDay(calendar)) {
            calendar.add(Calendar.DAY_OF_MONTH, -1);
        }

        calendar.add(Calendar.DAY_OF_YEAR, -1);
        DateUtils.setInclusiveEndTime(calendar);
        return calendar.getTime();
    }

    /**
     * Get the first date in a period depending on monthly adjusted period break date. This is
     * pre-calculated if it is contained in the map, otherwise it will be calculated for the given
     * parameters.
     */
    public static Date getFirstDateFromPeriod(String period, int breakDay) {
        if (FIRST_DAY_OF_PERIODS.containsKey(period)) {
            return FIRST_DAY_OF_PERIODS.get(period).get(breakDay);
        } else {
            Calendar calendarPeriod = DateUtils.getCalendar();
            int year = Integer.parseInt(period.substring(0, 4));
            int month = Integer.parseInt(period.substring(5));
            calendarPeriod.set(year, month - 1, 1);
            return getFirstDateHelper(calendarPeriod, breakDay);
        }
    }

    /**
     * Get the last date in a period depending on monthly adjusted period break date. This is
     * pre-calculated if it is contained in the map, otherwise it will be calculated for the given
     * parameters.
     */
    public static Date getLastDateFromPeriod(String period, int breakDay) {
        if (LAST_DAY_OF_PERIODS.containsKey(period)) {
            return LAST_DAY_OF_PERIODS.get(period).get(breakDay);
        } else {
            Calendar calendarPeriod = DateUtils.getCalendar();
            int year = Integer.parseInt(period.substring(0, 4));
            int month = Integer.parseInt(period.substring(5));
            calendarPeriod.set(year, month - 1, 1);
            return getLastDateHelper(calendarPeriod, breakDay);
        }
    }
}
