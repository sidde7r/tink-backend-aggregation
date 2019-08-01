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

/**
 * The generic date helper class that handles all date/time/holiday for various locale and timezone.
 * It is recommended to use in tink-backend-aggregation.
 */
public class CountryDateHelper {
    private Locale locale = new Locale("sv", "SE");
    private TimeZone timezone = TimeZone.getTimeZone("CET");
    private ImmutableSet<String> holidays;

    /** Default for Sweden */
    @Deprecated
    public CountryDateHelper() {
        this.holidays = getCountryHolidays(locale.getCountry());
    }

    /** Default timezone set to GMT if locale is UK otherwise CET */
    public CountryDateHelper(Locale locale) {
        this.locale = locale;

        if (locale.getCountry().equals("GB")) {
            this.timezone = TimeZone.getTimeZone("GMT");
        }

        this.holidays = getCountryHolidays(locale.getCountry());
    }

    public CountryDateHelper(Locale locale, TimeZone timezone) {
        this.locale = locale;
        this.timezone = timezone;

        this.holidays = getCountryHolidays(locale.getCountry());
    }

    public Date addDays(Date date, int days) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
        return calendar.getTime();
    }

    public Date addMonths(Date date, int months) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.MONTH, months);
        return calendar.getTime();
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

    public Calendar getCalendar(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return calendar;
    }

    private Calendar getCurrentOrNextBusinessDay(Calendar calendar) {
        Calendar businessDay = getCalendar(calendar.getTime());

        while (!isBusinessDay(businessDay)) {
            businessDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        return businessDay;
    }

    public Date getFutureBusinessDay(Date date, int numberOfBusinessDays) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        return getFutureBusinessDay(calendar, numberOfBusinessDays).getTime();
    }

    private Calendar getFutureBusinessDay(Calendar calendar, int numberOfBusinessDays) {
        Calendar futureBusinessDay = getCurrentOrNextBusinessDay(calendar);

        for (int i = 0; i < numberOfBusinessDays; i++) {
            futureBusinessDay.add(Calendar.DAY_OF_MONTH, 1);
            futureBusinessDay = getCurrentOrNextBusinessDay(futureBusinessDay);
        }

        return futureBusinessDay;
    }

    public Date getNextBusinessDay() {
        Date date = inclusiveEndTime(new Date());

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

    /** Returns today's date with start time (0.00). */
    public Date getTodayWithStartTime() {
        return inclusiveStartTime(new Date());
    }

    public void setInclusiveEndTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        calendar.set(Calendar.MILLISECOND, 999);
    }

    private void setInclusiveStartTime(Calendar calendar) {
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
    }

    public Date inclusiveEndTime(Date date) {
        Calendar calendar = getCalendar(date);
        setInclusiveEndTime(calendar);
        return calendar.getTime();
    }

    public Date inclusiveStartTime(Date date) {
        Calendar calendar = getCalendar(date);
        setInclusiveStartTime(calendar);
        return calendar.getTime();
    }

    public boolean isBeforeToday(Date date) {
        Date today = getTodayWithStartTime();

        return date.before(today);
    }

    public boolean isBusinessDay(Date date) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);

        return isBusinessDay(calendar);
    }

    /** Creates a calendar with the default Swedish timezone and locale. */
    public Calendar getCalendar() {
        return Calendar.getInstance(timezone, locale);
    }

    /** Returns whether a localDate is a business day or not. */
    public boolean isBusinessDay(LocalDate date) {
        return isBusinessDay(date.toDate());
    }

    /** Returns whether a calendar date is a business day or not. */
    public boolean isBusinessDay(Calendar calendar) {
        if (holidays.contains(
                ThreadSafeDateFormat.FORMATTER_INTEGER_DATE.format(calendar.getTime()))) {
            return false;
        }

        int dayOfWeek = calendar.get(Calendar.DAY_OF_WEEK);

        return (dayOfWeek != Calendar.SATURDAY && dayOfWeek != Calendar.SUNDAY);
    }

    private ImmutableSet<String> getCountryHolidays(String countryCode) {
        ImmutableSet.Builder<String> holidayBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<LocalDate> holidayLocalDateBuilder = ImmutableSet.builder();

        int year = LocalDate.now().getYear() - 20;

        HolidayCalendar calendar = HolidayCalendar.SWEDEN;
        if (countryCode.equals("BE")) {
            calendar = HolidayCalendar.BELGIUM;
        } else if (countryCode.equals("GB")) {
            calendar = HolidayCalendar.UNITED_KINGDOM;
        }

        HolidayManager holidayManager = HolidayManager.getInstance(calendar);

        for (int yearIdendex = year; yearIdendex < year + 25; yearIdendex++) {
            Set<Holiday> holidays = holidayManager.getHolidays(yearIdendex);
            for (Holiday holiday : holidays) {
                holidayLocalDateBuilder.add(holiday.getDate());

                if (countryCode.equals("SE")) {
                    getSwedishSpecificHolidays(holiday, holidayLocalDateBuilder);
                }
            }
        }

        for (LocalDate date : holidayLocalDateBuilder.build()) {
            holidayBuilder.add(date.toString("yyyyMMdd"));
        }

        return holidayBuilder.build();
    }

    private void getSwedishSpecificHolidays(
            Holiday holiday, ImmutableSet.Builder<LocalDate> holidayLocalDateBuilder) {
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
