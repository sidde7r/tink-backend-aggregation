package se.tink.libraries.date;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.temporal.ChronoUnit;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;
import java.util.regex.Pattern;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.LocalTime;

/** * @deprecated Use CountryDateHelper instead. */
@Deprecated
public final class DateUtils {
    private static final int MILLISECONDS_PER_DAY = 24 * 60 * 60 * 1000;
    static final Locale DEFAULT_LOCALE = new Locale("sv", "SE");
    private static final ZoneId DEFAULT_ZONE_ID = ZoneId.of("CET");
    private static final Pattern PATTERN_SIX_OR_EIGHT_DIGITS =
            Pattern.compile("[0-9]{6}([0-9]{2})?"); // Either 6 or 8 digits

    private DateUtils() {
        throw new AssertionError();
    }

    /** @return a Central European Time time zone instance (mutable) */
    static TimeZone createCetTimeZone() {
        return TimeZone.getTimeZone("CET");
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

    static Calendar getCalendar(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return calendar;
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

    /**
     * Returns today's date with flattened time (12.00).
     *
     * @return
     */
    public static Date getToday() {
        return flattenTime(new Date());
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

    public static Date addDays(Date date, int days) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
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
