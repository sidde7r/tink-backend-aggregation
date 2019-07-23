package se.tink.libraries.date;

import com.google.common.base.Strings;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.ImmutableSet.Builder;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import de.jollyday.Holiday;
import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Interval;
import org.joda.time.LocalDate;
import org.joda.time.LocalDateTime;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.joda.time.format.PeriodFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DateUtils {
    private static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final ImmutableSet<String> HOLIDAYS;
    private static final ImmutableSet<LocalDate> HOLIDAYS_LOCAL_DATE;
    private static final ImmutableSet<java.time.LocalDate> HOLIDAYS_JAVA_LOCAL_DATE;
    private static final Logger log = LoggerFactory.getLogger(DateUtils.class);
    private static final Pattern PATTERN_SIX_OR_EIGHT_DIGITS =
            Pattern.compile("[0-9]{6}([0-9]{2})?"); // Either 6 or 8
    // digits
    private static final DateTimeFormatter DATE_TIME_FORMATTER_MONTHLY =
            DateTimeFormat.forPattern("yyyy-MM");
    private static final java.time.format.DateTimeFormatter JAVA_LOCAL_DATE_MONTHLY_FORMATTER =
            java.time.format.DateTimeFormatter.ofPattern("yyyy-MM");
    private static final String WEEK_OF_YEAR_DATE_FORMAT = "yyyy:ww";

    static {
        // Create two sets with holidays, one with string representation and one with Joda time
        // LocalDate
        // (date without time), the later with higher performance for lookups from DateTime

        Builder<String> holidayBuilder = ImmutableSet.builder();
        Builder<LocalDate> holidayLocalDateBuilder = ImmutableSet.builder();

        HolidayManager holidayManager = HolidayManager.getInstance(HolidayCalendar.SWEDEN);

        int year = 2020;

        for (int i = 0; i < 30; i++) {
            Set<Holiday> holidays = holidayManager.getHolidays(year);

            for (Holiday holiday : holidays) {
                holidayLocalDateBuilder.add(holiday.getDate());

                // Add Christmas Eve
                if (holiday.getPropertiesKey().equals("CHRISTMAS")) {
                    holidayLocalDateBuilder.add(holiday.getDate().minusDays(1));
                }

                // Add Midsummer Eve
                if (holiday.getPropertiesKey().equals("MIDSUMMER")) {
                    holidayLocalDateBuilder.add(holiday.getDate().minusDays(1));
                }
            }
            year--;
        }

        HOLIDAYS_LOCAL_DATE = holidayLocalDateBuilder.build();

        Builder<java.time.LocalDate> javaLocalDateBuilder = ImmutableSet.builder();
        // Copy all dates to string represenation
        for (LocalDate date : HOLIDAYS_LOCAL_DATE) {
            holidayBuilder.add(date.toString("yyyyMMdd"));
            javaLocalDateBuilder.add(
                    java.time.LocalDate.of(
                            date.getYear(), date.getMonthOfYear(), date.getDayOfMonth()));
        }

        HOLIDAYS = holidayBuilder.build();
        HOLIDAYS_JAVA_LOCAL_DATE = javaLocalDateBuilder.build();
    }

    private DateUtils() {
        throw new AssertionError();
    }

    /** @return a Central European Time time zone instance (mutable) */
    static TimeZone createCetTimeZone() {
        return TimeZone.getTimeZone("CET");
    }

    /**
     * Helper function to create a MONTHLY period from year and month (as integers).
     *
     * @param year
     * @param month
     * @return
     */
    private static String createPeriod(int year, int month) {
        return year + "-" + Strings.padStart(Integer.toString(month), 2, '0');
    }

    static List<String> createPeriodListForYear(
            int year, ResolutionTypes resolution, int periodBreakDate) {

        try {
            Date startDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(year + "-01-01");
            Date endDate = ThreadSafeDateFormat.FORMATTER_DAILY.parse(year + "-12-01");
            return DateUtils.createPeriodList(startDate, endDate, resolution, periodBreakDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return Lists.newArrayList();
    }

    /** @return a list of periods based on a start date and end date (inclusive). */
    private static List<String> createPeriodList(
            Date startDate, Date endDate, ResolutionTypes resolution, int periodBreakDate) {

        if (startDate == null || endDate == null) {
            return Lists.newArrayList(getCurrentMonthPeriod(resolution, periodBreakDate));
        }

        try {
            List<String> periods = Lists.newArrayList();

            Date startPeriodDate =
                    ThreadSafeDateFormat.FORMATTER_MONTHLY.parse(
                            getMonthPeriod(startDate, resolution, periodBreakDate));
            Date endPeriodDate =
                    ThreadSafeDateFormat.FORMATTER_MONTHLY.parse(
                            getMonthPeriod(endDate, resolution, periodBreakDate));

            int startYear =
                    Integer.parseInt(ThreadSafeDateFormat.FORMATTER_YEARLY.format(startPeriodDate));
            int endYear =
                    Integer.parseInt(ThreadSafeDateFormat.FORMATTER_YEARLY.format(endPeriodDate));

            Integer startMonth =
                    Integer.valueOf(
                            ThreadSafeDateFormat.FORMATTER_MONTHLY_ONLY.format(startPeriodDate));
            Integer endMonth =
                    Integer.valueOf(
                            ThreadSafeDateFormat.FORMATTER_MONTHLY_ONLY.format(endPeriodDate));

            for (int y = startYear; y < endYear + 1; y++) {
                if (y == startYear && y == endYear) {
                    for (int m = startMonth; m < endMonth + 1; m++) {
                        periods.add(createPeriod(y, m));
                    }
                } else if (y == startYear) {
                    for (int m = startMonth; m < 13; m++) {
                        periods.add(createPeriod(y, m));
                    }
                } else if (y == endYear) {
                    for (int m = 1; m < endMonth + 1; m++) {
                        periods.add(createPeriod(y, m));
                    }
                } else {
                    for (int m = 1; m < 13; m++) {
                        periods.add(createPeriod(y, m));
                    }
                }
            }

            return periods;
        } catch (Exception e) {
            log.error("Could not create period list (" + startDate + ", " + endDate + ")", e);
            return Lists.newArrayList(getCurrentMonthPeriod(resolution, periodBreakDate));
        }
    }

    static List<Date> createDailyDateList(Date first, Date last) {
        return createDailyDateList(first, last, false);
    }

    static List<Date> createDailyDateList(Date first, Date last, boolean reverse) {
        List<Date> dates = Lists.newArrayList();

        int numDays = daysBetween(first, last);
        if (numDays > 0) {
            for (int i = 0; i <= numDays; i++) {
                Date tmp = org.apache.commons.lang3.time.DateUtils.addDays(first, i);
                dates.add(tmp);
            }
        }

        return reverse ? Lists.reverse(dates) : dates;
    }

    static List<String> createMonthlyPeriodList(String lastPeriod, int months) {
        LocalDateTime date = LocalDateTime.parse(lastPeriod, DATE_TIME_FORMATTER_MONTHLY);

        return IntStream.range(0, months)
                .mapToObj(
                        order ->
                                ThreadSafeDateFormat.FORMATTER_MONTHLY.format(
                                        date.minusMonths(order)))
                .collect(Collectors.toList());
    }

    /**
     * Flattens a date by setting the time of day to noon (used in the case where we don't get time
     * information from upstream information providers).
     *
     * @param date
     * @return
     */
    public static Date flattenTime(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /**
     * Creates a calendar with the default timezone and locale.
     *
     * @return
     */
    public static Calendar getCalendar() {
        return Calendar.getInstance(createCetTimeZone(), DEFAULT_LOCALE);
    }

    static Calendar getCalendar(Locale locale) {
        return Calendar.getInstance(createCetTimeZone(), locale);
    }

    static Calendar getCalendar(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Returns the current MONTHLY_ADJUSTED period based on the user's specified period break date.
     *
     * @return
     */
    private static String getCurrentMonthPeriod(ResolutionTypes resolution, int periodBreakDate) {
        return getMonthPeriod(getToday(), resolution, periodBreakDate);
    }

    static Period getPreviousPeriod(List<Period> periods, Period current) {
        if (current == null) {
            return null;
        }

        DateTime date = new DateTime(current.getStartDate()).minusDays(1);

        return getPeriodForDate(periods, date.toDate());
    }

    private static Period getPeriodForDate(List<Period> periods, final Date date) {

        if (date == null || periods == null || periods.isEmpty()) {
            return null;
        }

        return Iterables.find(periods, period -> period.isDateWithin(date), null);
    }

    /**
     * Returns the current period progress (how far into a period we currently are at).
     *
     * @param resolution
     * @param periodBreakDate
     * @return
     */
    static double getCurrentMonthPeriodProgress(ResolutionTypes resolution, int periodBreakDate) {
        String currentMonthPeriod = getCurrentMonthPeriod(resolution, periodBreakDate);
        Date date = new Date();

        return getMonthPeriodProgress(currentMonthPeriod, date, resolution, periodBreakDate);
    }

    /**
     * Return the current or previous business day for a date (ie. a sunday returns the previous
     * friday's date, and an actual business day returns the same date).
     *
     * @param date
     * @return
     */
    public static Date getCurrentOrPreviousBusinessDay(Date date) {
        return getCurrentOrPreviousBusinessDay(getCalendar(date)).getTime();
    }

    private static Calendar getCurrentOrPreviousBusinessDay(Calendar calendar) {
        Calendar businessDay = getCalendar(calendar.getTime());

        while (!isBusinessDay(businessDay)) {
            businessDay.add(Calendar.DAY_OF_MONTH, -1);
        }

        return businessDay;
    }

    /**
     * @param date
     * @return
     */
    public static Date getCurrentOrNextBusinessDay(Date date) {
        return getCurrentOrNextBusinessDay(getCalendar(date)).getTime();
    }

    public static Calendar getCurrentOrNextBusinessDay(Calendar calendar) {
        Calendar businessDay = getCalendar(calendar.getTime());

        while (!isBusinessDay(businessDay)) {
            businessDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        return businessDay;
    }

    public static Date getCurrentOrNextBusinessDay() {
        Date date = DateUtils.setInclusiveStartTime(new Date());

        return getCurrentOrNextBusinessDay(date);
    }

    public static Date getNextBusinessDay() {
        Date date = DateUtils.inclusiveEndTime(new Date());

        return getNextBusinessDay(date);
    }

    public static Date getNextBusinessDay(Date date) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        return getCurrentOrNextBusinessDay(calendar).getTime();
    }

    /**
     * Returns the first date of a period (MONTHLY).
     *
     * @param period
     * @return
     */
    public static Date getFirstDateFromPeriod(String period) {
        return getFirstDateFromPeriod(period, ResolutionTypes.MONTHLY, -1);
    }

    /**
     * Returns the first date of a period based on the users MONTHLY_ADJUSTED settings.
     *
     * @param period
     * @param resolution
     * @param periodBreakDate
     * @return
     */
    static Date getFirstDateFromPeriod(
            String period, ResolutionTypes resolution, int periodBreakDate) {
        Calendar calendar = getCalendar();

        int year = Integer.parseInt(period.substring(0, 4));
        int month = Integer.parseInt(period.substring(5));

        calendar.set(year, month - 1, 1);

        if (resolution == ResolutionTypes.MONTHLY_ADJUSTED) {
            calendar.add(Calendar.MONTH, -1);
            calendar.set(
                    Calendar.DATE,
                    Math.min(periodBreakDate, calendar.getActualMaximum(Calendar.DATE)));

            while (!isBusinessDay(calendar)) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }
        }

        DateUtils.setInclusiveStartTime(calendar);

        return calendar.getTime();
    }

    /**
     * Returns the first date of a list of periods based on the users MONTHLY_ADJUSTED settings.
     *
     * @param periods
     * @param resolution
     * @param breakDate
     * @return
     */
    static Date getFirstDateFromPeriods(
            List<String> periods, ResolutionTypes resolution, int breakDate) {

        if (periods.size() != 0) {
            return getFirstDateFromPeriod(getFirstPeriod(periods), resolution, breakDate);
        }

        return getFirstDateFromPeriod(
                getCurrentMonthPeriod(resolution, breakDate), resolution, breakDate);
    }

    /**
     * Returns the first period from a list of periods.
     *
     * @param periods
     * @return
     */
    private static String getFirstPeriod(List<String> periods) {
        return periods.stream().min(Comparator.naturalOrder()).get();
    }

    /**
     * Returns the last date of a period (MONTHLY).
     *
     * @param period
     * @return
     */
    public static Date getLastDateFromPeriod(String period) {
        return getLastDateFromPeriod(period, ResolutionTypes.MONTHLY, -1);
    }

    /**
     * Returns the last date of a period based on the users MONTHLY_ADJUSTED settings.
     *
     * @param period
     * @param resolution
     * @param breakDate
     * @return
     */
    static Date getLastDateFromPeriod(String period, ResolutionTypes resolution, int breakDate) {
        Calendar calendar = getCalendar();

        int year = Integer.parseInt(period.substring(0, 4));
        int month = Integer.parseInt(period.substring(5));

        calendar.set(year, month - 1, 1);
        calendar.set(Calendar.DATE, calendar.getActualMaximum(Calendar.DATE));

        if (resolution == ResolutionTypes.MONTHLY_ADJUSTED) {
            calendar.set(
                    Calendar.DATE, Math.min(breakDate, calendar.getActualMaximum(Calendar.DATE)));

            while (!isBusinessDay(calendar)) {
                calendar.add(Calendar.DAY_OF_MONTH, -1);
            }

            calendar.add(Calendar.DAY_OF_YEAR, -1);
        }

        DateUtils.setInclusiveEndTime(calendar);

        return calendar.getTime();
    }

    /**
     * Returns the first date of a list of period based on the users MONTHLY_ADJUSTED settings.
     *
     * @param periods
     * @param resolution
     * @param breakDate
     * @return
     */
    static Date getLastDateFromPeriods(
            List<String> periods, ResolutionTypes resolution, int breakDate) {

        if (periods.size() != 0) {
            return getLastDateFromPeriod(getLastPeriod(periods), resolution, breakDate);
        }

        return getLastDateFromPeriod(
                getCurrentMonthPeriod(resolution, breakDate), resolution, breakDate);
    }

    /**
     * Returns the last period from a list of periods.
     *
     * @param periods
     * @return
     */
    private static String getLastPeriod(List<String> periods) {
        return periods.stream().max(Comparator.naturalOrder()).get();
    }

    /**
     * Returns the MONTHLY period for a date.
     *
     * @param time
     * @return
     */
    public static String getMonthPeriod(Date time) {
        return getMonthPeriod(time, ResolutionTypes.MONTHLY, -1);
    }

    /** Returns the MONTHLY/MONTHLY_ADJUSTED period for a date. */
    static String getMonthPeriod(Date date, ResolutionTypes resolution, int periodBreakDate) {
        if (resolution == ResolutionTypes.MONTHLY) {
            return ThreadSafeDateFormat.FORMATTER_MONTHLY.format(date);
        }
        java.time.LocalDate localDate = getPeriodDate(date, periodBreakDate);

        return localDate.format(JAVA_LOCAL_DATE_MONTHLY_FORMATTER);
    }

    private static java.time.LocalDate getPeriodDate(Date date, int periodBreakDate) {
        // Find last not business day after this date or take this date.
        // This is reversed action for finding first day of a period.
        java.time.LocalDate localDate = date.toInstant().atZone(DEFAULT_ZONE_ID).toLocalDate();
        localDate = localDate.plusDays(1);

        while (!isBusinessDay(localDate)) {
            localDate = localDate.plusDays(1);
        }
        localDate = localDate.minusDays(1);
        int dayOfMonth = localDate.getDayOfMonth();

        // If current day after period break day, date is in the next month period
        if (dayOfMonth >= periodBreakDate
                || dayOfMonth == localDate.getMonth().length(localDate.isLeapYear())) {
            localDate = localDate.plusMonths(1);
        }
        return localDate;
    }

    static String getNextMonthPeriod(String period) {
        try {
            LocalDateTime date = LocalDateTime.parse(period, DATE_TIME_FORMATTER_MONTHLY);
            return ThreadSafeDateFormat.FORMATTER_MONTHLY.format(date.plusMonths(1));
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Wrong date format. Period must be on format 'yyyy-MM'. Period: " + period, e);
        }
    }

    static String getPreviousMonthPeriod(String period) {
        try {
            LocalDateTime date = LocalDateTime.parse(period, DATE_TIME_FORMATTER_MONTHLY);
            return ThreadSafeDateFormat.FORMATTER_MONTHLY.format(date.minusMonths(1));
        } catch (RuntimeException e) {
            throw new RuntimeException(
                    "Wrong date format. Period must be on format 'yyyy-MM'. Period: " + period, e);
        }
    }

    /**
     * Returns the period progress (how far into a period we currently are at).
     *
     * @param resolution
     * @param periodBreakDate
     * @return
     */
    private static double getMonthPeriodProgress(
            String period, Date date, ResolutionTypes resolution, int periodBreakDate) {
        Date startDate = getFirstDateFromPeriod(period, resolution, periodBreakDate);
        Date endDate = getLastDateFromPeriod(period, resolution, periodBreakDate);

        return ((double) (date.getTime() - startDate.getTime())
                / (double) (endDate.getTime() - startDate.getTime()));
    }

    /**
     * Returns the number of days between two dates.
     *
     * @param d1
     * @param d2
     * @return
     */
    public static int getNumberOfDaysBetween(Date d1, Date d2) {
        return (int) ((d2.getTime() - d1.getTime()) / (1000 * 60 * 60 * 24));
    }

    /**
     * Returns the number of months between two dates.
     *
     * @param d1
     * @param d2
     * @return
     */
    public static double getNumberOfMonthsBetween(Date d1, Date d2) {
        DateTime dateTime1 = new DateTime(d1);
        DateTime dateTime2 = new DateTime(d2);

        return Days.daysBetween(dateTime1, dateTime2).getDays() / 30d;
    }

    public static String toISO8601Format(Date date) {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'");
        df.setTimeZone(tz);
        return df.format(date);
    }

    /**
     * Returns today's date with flattened time (12.00).
     *
     * @return
     */
    public static Date getToday() {
        return flattenTime(new Date());
    }

    /**
     * Returns whether a date is a business day or not.
     *
     * @param calendar
     * @return
     */
    public static boolean isBusinessDay(Calendar calendar) {
        if (HOLIDAYS.contains(
                ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(calendar.getTime()))) {
            return false;
        }

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
    }

    private static boolean isBusinessDay(java.time.LocalDate date) {
        if (HOLIDAYS_JAVA_LOCAL_DATE.contains(date)) {
            return false;
        }

        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return !dayOfWeek.equals(DayOfWeek.SATURDAY) && !dayOfWeek.equals(DayOfWeek.SUNDAY);
    }

    /**
     * Returns whether a date is a business day or not.
     *
     * @param date
     * @return
     */
    public static boolean isBusinessDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);

        return isBusinessDay(calendar);
    }

    /**
     * Returns whether two dates are the same day or not.
     *
     * @param date1
     * @param date2
     * @return
     */
    public static boolean isSameDay(Date date1, Date date2) {
        if (date1 == null || date2 == null) {
            return false;
        }

        return daysBetween(date1, date2) == 0;
    }

    /**
     * Magically parses a date string (using Joda).
     *
     * @param text
     * @return
     */
    public static Date parseDate(String text) {
        return new org.pojava.datetime.DateTime(text.trim()).toDate();
    }

    static void setInclusiveEndTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    static Date inclusiveEndTime(Date date) {
        Calendar calendar = getCalendar(date);
        setInclusiveEndTime(calendar);
        return calendar.getTime();
    }

    public static void setInclusiveStartTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public static Date setInclusiveStartTime(Date date) {
        Calendar calendar = getCalendar(date);
        setInclusiveStartTime(calendar);
        return calendar.getTime();
    }

    static Date setInclusiveEndTime(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);

        setInclusiveEndTime(calendar);

        return calendar.getTime();
    }

    /**
     * Uses {@link #turnPastSixDigitsDateIntoEightDigits(java.text.DateFormat, java.text.DateFormat,
     * String)} with defualt values for inFormat (yyMMdd) and outFormat (yyyyMMdd)
     *
     * @param s
     * @return
     * @throws ParseException
     */
    public static String turnPastSixDigitsDateIntoEightDigits(String s) throws ParseException {
        DateFormat formatIn;
        if (!PATTERN_SIX_OR_EIGHT_DIGITS.matcher(s).matches()) {
            throw new ParseException("Personnummer doesn't parse", 0);
        }

        if (s.length() == 6) {
            formatIn = new SimpleDateFormat("yyMMdd");
        } else {
            formatIn = new SimpleDateFormat("yyyyMMdd");
        }

        return turnPastSixDigitsDateIntoEightDigits(formatIn, new SimpleDateFormat("yyyyMMdd"), s);
    }

    /**
     * Turns dates on format yyMMdd into yyyMMdd. Can only be past dates, because it adds the
     * century 19 or 20. It chooses later dates before earlier (i.e. 011231 would become 20011231
     * instead of 19011231), however only past.
     *
     * @param inFormat
     * @param outFormat
     * @param s
     * @return
     * @throws ParseException
     */
    private static String turnPastSixDigitsDateIntoEightDigits(
            DateFormat inFormat, DateFormat outFormat, String s) throws ParseException {
        Date date = inFormat.parse(s);

        // Validation. Apparently 31st nov silently becomes 1st dec instead of ParseException.
        String s2 = inFormat.format(date);
        if (!s.equals(s2)) {
            throw new ParseException("Date isn't valid", 0);
        }

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);

        Calendar now = Calendar.getInstance();
        if (cal.after(now)) {
            cal.add(Calendar.YEAR, -100);
        }

        return outFormat.format(cal.getTime());
    }

    public static int daysBetween(Date date1, Date date2) {
        long days1 =
                (date1.getTime() + createCetTimeZone().getOffset(date1.getTime()))
                        / MILLISECONDS_PER_DAY;
        long days2 =
                (date2.getTime() + createCetTimeZone().getOffset(date2.getTime()))
                        / MILLISECONDS_PER_DAY;

        return (int) (days2 - days1);
    }

    static Calendar getFirstDateOfWeek(Calendar calendar) {
        Calendar weekStartCalendar = (Calendar) calendar.clone();
        weekStartCalendar.add(
                Calendar.DAY_OF_WEEK,
                (weekStartCalendar.getFirstDayOfWeek()
                                - weekStartCalendar.get(Calendar.DAY_OF_WEEK)
                                - 7)
                        % 7);
        setInclusiveStartTime(weekStartCalendar);
        return weekStartCalendar;
    }

    static boolean isBusinessDayWithinDaysFromNow(Date date, int days) {
        Date businessDay = DateUtils.getCurrentOrPreviousBusinessDay(DateUtils.daysFromNow(days));

        return inclusiveEndTime(date).before(businessDay);
    }

    public static Date max(Date date1, Date date2) {
        if (compare(date1, date2) < 0) {
            return date2;
        } else {
            return date1;
        }
    }

    public static Date min(Date date1, Date date2) {
        if (compare(date1, date2) > 0) {
            return date2;
        } else {
            return date1;
        }
    }

    public static int compare(Date date1, Date date2) {

        if (date1 == null && date2 == null) {
            return 0;
        }

        if (date1 == null) {
            return -1;
        }

        if (date2 == null) {
            return 1;
        }

        if (date1.before(date2)) {
            return -1;
        }

        if (date1.after(date2)) {
            return 1;
        }

        return 0;
    }

    public static Date addDays(Date date, int days) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    static Date daysFromNow(int days) {
        Date today = inclusiveEndTime(new Date());
        return addDays(today, days);
    }

    public static Date addMonths(Date date, int months) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
    }

    public static Date addYears(Date date, int years) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.YEAR, years);
        return calendar.getTime();
    }

    static String prettyFormatMillis(int millis) {

        String sign = millis < 0 ? "-" : "";

        DateTime now = new DateTime();
        return sign
                + PeriodFormat.getDefault()
                        .print(new Interval(now, now.plusMillis(Math.abs(millis))).toPeriod());
    }

    static Period buildDailyPeriod(String stringPeriod) {
        Date date = parseDate(stringPeriod);

        Period period = new Period();
        period.setStartDate(setInclusiveStartTime(date));
        period.setEndDate(setInclusiveEndTime(date));
        period.setName(stringPeriod);
        period.setResolution(ResolutionTypes.DAILY);

        return period;
    }

    static Period buildWeeklyPeriod(String stringPeriod, Locale locale) {
        Date date;
        try {
            date = new SimpleDateFormat(WEEK_OF_YEAR_DATE_FORMAT, locale).parse(stringPeriod);
        } catch (ParseException e) {
            log.error("Cannot parse week: " + stringPeriod, e);
            return null;
        }

        Period period = new Period();
        period.setStartDate(setInclusiveStartTime(date));
        period.setEndDate(setInclusiveEndTime(addDays(date, 6)));
        period.setName(stringPeriod);
        period.setResolution(ResolutionTypes.WEEKLY);

        return period;
    }

    static Period buildMonthlyPeriod(
            String period, ResolutionTypes resolutionType, int periodBreakDay) {
        period = period.substring(0, 7); // yyyy-mm
        Period newPeriod = new Period();
        newPeriod.setStartDate(getFirstDateFromPeriod(period, resolutionType, periodBreakDay));
        newPeriod.setEndDate(getLastDateFromPeriod(period, resolutionType, periodBreakDay));
        newPeriod.setName(period);
        newPeriod.setResolution(resolutionType);

        return newPeriod;
    }

    static Period buildYearlyPeriod(int year, ResolutionTypes periodMode, int periodBreakDay) {
        Period period = new Period();
        period.setStartDate(getFirstDateFromPeriod(year + "-01", periodMode, periodBreakDay));
        period.setEndDate(getLastDateFromPeriod(year + "-12", periodMode, periodBreakDay));
        period.setName(Integer.toString(year));
        period.setResolution(ResolutionTypes.YEARLY);

        return period;
    }

    static String getYearlyPeriod(String period, ResolutionTypes periodMode, int periodBreakDay) {
        if (ThreadSafeDateFormat.FORMATTER_YEARLY.fitsFormat(period)) {
            return period;
        }

        if (ThreadSafeDateFormat.FORMATTER_MONTHLY.fitsFormat(period)) {
            return period.substring(0, 4);
        }

        if (ThreadSafeDateFormat.FORMATTER_WEEKLY.fitsFormat(period)) {
            return period.substring(0, 4);
        }

        if (ThreadSafeDateFormat.FORMATTER_DAILY.fitsFormat(period)) {
            return getMonthPeriod(parseDate(period), periodMode, periodBreakDay).substring(0, 4);
        }
        // others
        throw new RuntimeException(
                new ParseException("Could not parse year from period: " + period, 0));
    }

    /**
     * Will return true if the value is within the interval with midnight overlap. Examples: 02:00
     * will be within interval 01:00 - 05:00 00:00 will be within interval 21:00 - 03:00
     */
    public static boolean isWithinClosedInterval(LocalTime from, LocalTime to, LocalTime time) {
        if (from.isAfter(to)) {
            // Return true if the time is after (or at) start, or it's before end
            return time.compareTo(from) >= 0 || time.compareTo(to) < 0;
        } else {
            return from.compareTo(time) <= 0 && time.compareTo(to) < 0;
        }
    }

    public static long getCalendarMonthsBetween(Date date1, Date date2) {
        final TimeZone cetTimeZone = createCetTimeZone();
        YearMonth m1 = YearMonth.from(date1.toInstant().atZone(cetTimeZone.toZoneId()));
        YearMonth m2 = YearMonth.from(date2.toInstant().atZone(cetTimeZone.toZoneId()));
        return m1.until(m2, ChronoUnit.MONTHS);
    }

    public static boolean before(Date date1, Date date2) {
        return date1.compareTo(date2) < 0;
    }

    public static boolean after(Date date1, Date date2) {
        return date1.compareTo(date2) > 0;
    }

    public static java.time.LocalDate toJavaTimeLocalDate(java.util.Date date) {
        if (date != null) {
            return Instant.ofEpochMilli(date.getTime()).atZone(DEFAULT_ZONE_ID).toLocalDate();
        }
        return null;
    }

    public static java.util.Date toJavaUtilDate(java.time.LocalDate date) {
        return new java.util.Date(
                date.atTime(java.time.LocalTime.NOON)
                        .atZone(DEFAULT_ZONE_ID)
                        .toInstant()
                        .toEpochMilli());
    }

    public static java.util.Date toJavaUtilDate(
            CharSequence date, java.time.format.DateTimeFormatter formatter) {
        return toJavaUtilDate(java.time.LocalDate.parse(date, formatter));
    }
}
