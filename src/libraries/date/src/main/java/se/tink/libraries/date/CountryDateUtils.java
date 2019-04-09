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
import org.joda.time.LocalDate;

public class CountryDateUtils {
    protected Locale DEFAULT_LOCALE;
    private static final TimeZone DEFAULT_TIMEZONE = TimeZone.getTimeZone("CET");
    private final ImmutableSet<String> HOLIDAYS;

    private static CountryDateUtils BE_DATE_UTILS =
            new CountryDateUtils(HolidayCalendar.BELGIUM, new Locale("fr", "BE"));
    private static CountryDateUtils SV_DATE_UTILS =
            new CountryDateUtils(HolidayCalendar.SWEDEN, new Locale("sv", "SE"));

    public CountryDateUtils(HolidayCalendar country, Locale countryLocale) {
        // this.holidayManager = HolidayManager.getInstance(country);
        this.DEFAULT_LOCALE = countryLocale;

        ImmutableSet.Builder<String> holidayBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<LocalDate> holidayLocalDateBuilder = ImmutableSet.builder();

        int year = LocalDate.now().getYear() - 20;

        HolidayManager holidayManager = HolidayManager.getInstance(country);
        for (int yearIdendex = year; yearIdendex < year + 25; yearIdendex++) {
            Set<Holiday> holidays = holidayManager.getHolidays(yearIdendex);
            for (Holiday holiday : holidays) {
                holidayLocalDateBuilder.add(holiday.getDate());
                getCountrySpecificHolidays(holiday, holidayLocalDateBuilder);
            }
        }

        ImmutableSet.Builder<java.time.LocalDate> javaLocalDateBuilder = ImmutableSet.builder();
        // Copy all dates to string represenation
        for (LocalDate date : holidayLocalDateBuilder.build()) {
            holidayBuilder.add(date.toString("yyyyMMdd"));
            javaLocalDateBuilder.add(
                    java.time.LocalDate.of(
                            date.getYear(), date.getMonthOfYear(), date.getDayOfMonth()));
        }

        HOLIDAYS = holidayBuilder.build();
    }

    public static CountryDateUtils getSwedishDateUtils() {
        return SV_DATE_UTILS;
    }

    public static CountryDateUtils getBelgianDateUtils() {
        return BE_DATE_UTILS;
    }

    private void getCountrySpecificHolidays(
            Holiday holiday, ImmutableSet.Builder<LocalDate> holidayLocalDateBuilder) {
        if (DEFAULT_LOCALE.getCountry().equals("SE")) {
            // Add Christmas Eve
            if (holiday.getPropertiesKey().equals("CHRISTMAS")) {
                holidayLocalDateBuilder.add(holiday.getDate().minusDays(1));
            }

            // Add Midsummer Eve
            if (holiday.getPropertiesKey().equals("MIDSUMMER")) {
                holidayLocalDateBuilder.add(holiday.getDate().minusDays(1));
            }
        }
    }

    /**
     * Flattens a date by setting the time of day to noon (used in the case where we don't get time
     * information from upstream information providers).
     */
    public Date flattenTime(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);

        calendar.set(Calendar.HOUR_OF_DAY, 12);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        return calendar.getTime();
    }

    /** Creates a calendar with the default timezone and locale. */
    public Calendar getCalendar() {
        return Calendar.getInstance(DEFAULT_TIMEZONE, DEFAULT_LOCALE);
    }

    public Calendar getCalendar(Locale locale) {
        return Calendar.getInstance(DEFAULT_TIMEZONE, locale);
    }

    public Calendar getCalendar(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return calendar;
    }

    /**
     * Return the current or previous business day for a date (ie. a sunday returns the previous
     * friday's date, and an actual business day returns the same date).
     */
    public Date getCurrentOrPreviousBusinessDay(Date date) {
        return getCurrentOrPreviousBusinessDay(getCalendar(date)).getTime();
    }

    private Calendar getCurrentOrPreviousBusinessDay(Calendar calendar) {
        Calendar businessDay = getCalendar(calendar.getTime());

        while (!isBusinessDay(businessDay)) {
            businessDay.add(Calendar.DAY_OF_MONTH, -1);
        }

        return businessDay;
    }

    private Date getCurrentOrNextBusinessDay(Date date) {
        return getCurrentOrNextBusinessDay(getCalendar(date)).getTime();
    }

    private Calendar getCurrentOrNextBusinessDay(Calendar calendar) {
        Calendar businessDay = getCalendar(calendar.getTime());

        while (!isBusinessDay(businessDay)) {
            businessDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        return businessDay;
    }

    public Date getCurrentOrNextBusinessDay() {
        Date date = DateUtils.setInclusiveStartTime(new Date());

        return getCurrentOrNextBusinessDay(date);
    }

    public Date getNextBusinessDay() {
        Date date = DateUtils.inclusiveEndTime(new Date());

        return getNextBusinessDay(date);
    }

    public Date getNextBusinessDay(Date date) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        return getCurrentOrNextBusinessDay(calendar).getTime();
    }

    /** Returns today's date with flattened time (12.00). */
    public Date getToday() {
        return flattenTime(new Date());
    }

    /** Returns whether a date is a business day or not. */
    public boolean isBusinessDay(Calendar calendar) {
        if (HOLIDAYS.contains(
                ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(calendar.getTime()))) {
            return false;
        }

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
    }

    /**
     * Returns whether a date is a business day or not.
     *
     * @param date
     * @return
     */
    public boolean isBusinessDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);

        return isBusinessDay(calendar);
    }
}
