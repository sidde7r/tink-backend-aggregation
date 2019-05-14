package se.tink.libraries.date;

import com.google.common.collect.ImmutableSet;
import de.jollyday.Holiday;
import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.stream.IntStream;
import org.joda.time.LocalDate;

public class BelgianDateUtils {
    private static final Locale DEFAULT_LOCALE = new Locale("nl", "BE");
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("CET");
    private static final ImmutableSet<String> HOLIDAYS;

    static {
        ImmutableSet.Builder<String> holidayBuilder = ImmutableSet.builder();

        int year = LocalDate.now().getYear() - 20;

        HolidayManager holidayManager = HolidayManager.getInstance(HolidayCalendar.BELGIUM);

        IntStream.range(year, year + 25)
                .forEach(
                        yearIndex -> {
                            Set<Holiday> holidays = holidayManager.getHolidays(yearIndex);
                            holidays.stream()
                                    .map(holiday -> holiday.getDate().toString("yyyyMMdd"))
                                    .forEach(
                                            dateString -> {
                                                holidayBuilder.add(dateString);
                                            });
                        });

        HOLIDAYS = holidayBuilder.build();
    }

    /**
     * Flattens a date by setting the time of day to noon (used in the case where we don't get time
     * information from upstream information providers).
     */
    private static Date flattenTime(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /** Creates a calendar with the default timezone and locale. */
    private static Calendar getCalendar() {
        return Calendar.getInstance(DEFAULT_TIMEZONE, DEFAULT_LOCALE);
    }

    private static Calendar getCalendar(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return calendar;
    }

    public static Calendar getCurrentOrNextBusinessDay(Calendar calendar) {
        Calendar businessDay = getCalendar(calendar.getTime());

        while (!isBusinessDay(businessDay)) {
            businessDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        return businessDay;
    }

    public static Date getNextBusinessDay(Date date) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        return getCurrentOrNextBusinessDay(calendar).getTime();
    }

    /** Returns today's date with flattened time (12.00). */
    public static Date getToday() {
        return flattenTime(new Date());
    }

    /** Returns whether a date is a business day or not. */
    public static boolean isBusinessDay(Calendar calendar) {
        if (HOLIDAYS.contains(
                ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(calendar.getTime()))) {
            return false;
        }

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
    }

    /** Returns whether a date is a business day or not. */
    public static boolean isBusinessDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);

        return isBusinessDay(calendar);
    }
}
