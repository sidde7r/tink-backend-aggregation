package se.tink.libraries.date;

import com.google.common.collect.ImmutableSet;
import de.jollyday.Holiday;
import de.jollyday.HolidayCalendar;
import de.jollyday.HolidayManager;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalTime;
import java.time.ZonedDateTime;
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
    private Locale locale = new Locale(LANGUAGE_CODE_SWEDISH, COUNTRY_CODE_SWEDEN);
    private TimeZone timezone = TimeZone.getTimeZone(TIMEZONE_CODE_CET);
    private ImmutableSet<String> holidays;

    public static final String COUNTRY_CODE_SWEDEN = "SE";
    public static final String COUNTRY_CODE_BELGIUM = "BE";
    public static final String COUNTRY_CODE_GB = "GB";
    public static final String COUNTRY_CODE_PORTUGAL = "PT";

    public static final String LANGUAGE_CODE_SWEDISH = "sv";
    public static final String LANGUAGE_CODE_PORTUGAL = "pt";

    public static final String TIMEZONE_CODE_CET = "CET";
    public static final String TIMEZONE_CODE_GMT = "GMT";

    private Clock clock;

    /** Default for Sweden */
    @Deprecated
    public CountryDateHelper() {
        this.clock = Clock.system(timezone.toZoneId());
        this.holidays = getCountryHolidays(locale.getCountry());
    }

    /** Default timezone set to GMT if locale is UK otherwise CET */
    public CountryDateHelper(Locale locale) {
        this.locale = locale;

        if (locale.getCountry().equals(COUNTRY_CODE_GB)
                || locale.getCountry().equals(COUNTRY_CODE_PORTUGAL)) {
            this.timezone = TimeZone.getTimeZone(TIMEZONE_CODE_GMT);
        }

        this.clock = Clock.system(timezone.toZoneId());

        this.holidays = getCountryHolidays(locale.getCountry());
    }

    public CountryDateHelper(Locale locale, TimeZone timezone) {
        this.locale = locale;
        this.timezone = timezone;
        this.clock = Clock.system(timezone.toZoneId());
        this.holidays = getCountryHolidays(locale.getCountry());
    }

    public void setClock(Clock clock) {
        this.clock = clock;
    }

    public Clock getClock() {
        return clock;
    }

    public Date addDays(Date date, int days) {
        Calendar calendar = getCalendar();
        calendar.setTime(date);
        calendar.add(Calendar.DAY_OF_YEAR, days);
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

    public Calendar getCurrentOrNextBusinessDay(Calendar calendar) {
        Calendar businessDay = getCalendar(calendar.getTime());

        while (!isBusinessDay(businessDay)) {
            businessDay.add(Calendar.DAY_OF_MONTH, 1);
        }

        return businessDay;
    }

    private Date getNow() {
        return Date.from(clock.instant());
    }

    public java.time.LocalDate getNowAsLocalDate() {
        return java.time.LocalDate.now(clock);
    }

    public Date getNowAsDate() {
        return getNow();
    }

    public Date getNextBusinessDay() {
        Date date = inclusiveEndTime(getNow());

        return getNextBusinessDay(date);
    }

    public Date getNextBusinessDay(Date date) {
        Calendar calendar = getCalendar(date);
        calendar.add(Calendar.DAY_OF_MONTH, 1);

        return getCurrentOrNextBusinessDay(calendar).getTime();
    }

    /** Returns today's date with flattened time (12.00). */
    public Date getToday() {
        return flattenTime(getNow());
    }

    /** Returns today's date with start time (0.00). */
    public Date getTodayWithStartTime() {
        return inclusiveStartTime(getNow());
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
        Calendar calendar = Calendar.getInstance(timezone, locale);
        calendar.setTime(getNow());
        return calendar;
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

    /**
     * If providedDate is business day it will return the current date, otherwise return next
     * business day
     *
     * @param providedDate the {@link java.time.LocalDate} to consider
     * @return current {@link java.time.LocalDate} or providedDate
     */
    public java.time.LocalDate getCurrentOrNextBusinessDay(java.time.LocalDate providedDate) {
        Calendar calendar = getCalendar();
        calendar.setTimeInMillis(
                providedDate.atStartOfDay(timezone.toZoneId()).toInstant().toEpochMilli());
        Date date = getCurrentOrNextBusinessDay(calendar).getTime();
        return date.toInstant().atZone(timezone.toZoneId()).toLocalDate();
    }

    /**
     * If providedDate is null it will return the current date, otherwise return providedDate
     *
     * @param providedDate the {@link java.time.LocalDate} to consider
     * @return current {@link java.time.LocalDate} or providedDate
     */
    public java.time.LocalDate getProvidedDateOrCurrentLocalDate(java.time.LocalDate providedDate) {
        return providedDate == null ? getNowAsLocalDate() : providedDate;
    }

    /**
     * If providedDate is null it will return the current date, otherwise return providedDate
     *
     * @param providedDate the date to consider
     * @return current date or providedDate
     */
    public Date getProvidedDateOrCurrentDate(Date providedDate) {
        return providedDate == null ? getNow() : providedDate;
    }

    /**
     * If providedDate is not null it will return providedDate otherwise it will calculate the next
     * date that is not a holiday
     *
     * @param providedDate the date to consider as {@link java.time.LocalDate}
     * @param cutOffHours if the time of day is past this hour, the returned date will be the next
     *     business day.
     * @param cutOffMinutes if the time of day is past this minute, the returned date will be the
     *     next business day.
     * @return the next available business day or providedDate as {@link java.time.LocalDate}
     */
    public java.time.LocalDate getProvidedDateOrBestPossibleLocalDate(
            java.time.LocalDate providedDate, int cutOffHours, int cutOffMinutes) {
        Date providedDateAsLocalDate =
                providedDate == null
                        ? null
                        : Date.from(
                                providedDate
                                        .atStartOfDay()
                                        .atZone(timezone.toZoneId())
                                        .toInstant());
        return getProvidedDateOrBestPossibleDate(
                        providedDateAsLocalDate, cutOffHours, cutOffMinutes)
                .toInstant()
                .atZone(timezone.toZoneId())
                .toLocalDate();
    }

    /**
     * If providedDate is not null it will return providedDate otherwise it will calculate the next
     * date that is not a holiday
     *
     * @param providedDate the date to consider
     * @param cutOffHours if the time of day is past this hour, the returned date will be the next
     *     business day.
     * @param cutOffMinutes if the time of day is past this minute, the returned date will be the
     *     next business day.
     * @return the next available business day or providedDate
     */
    public Date getProvidedDateOrBestPossibleDate(
            Date providedDate, int cutOffHours, int cutOffMinutes) {
        return providedDate == null
                ? getBestPossibleTransferDate(cutOffHours, cutOffMinutes)
                : providedDate;
    }

    /**
     * If zoneDateTime falls between cutOff and cutOff - cutOffWindow (in seconds)
     *
     * @param zoneDateTime dateTime to check
     * @param cutOffHours the hour of the cutoff
     * @param cutOffMinutes the minute of the cutoff
     * @param cutOffWindow if between cutoff and cutoff - cutOffWindow (in seconds) return true
     * @return true if falls in between cutOff window
     */
    public boolean calculateIfWithinCutOffTime(
            ZonedDateTime zoneDateTime, int cutOffHours, int cutOffMinutes, int cutOffWindow) {

        Duration duration =
                Duration.between(
                        zoneDateTime,
                        ZonedDateTime.of(
                                java.time.LocalDate.now(timezone.toZoneId()),
                                LocalTime.of(cutOffHours, cutOffMinutes),
                                timezone.toZoneId()));
        return duration.getSeconds() < cutOffWindow && !duration.isNegative();
    }

    /**
     * If localDate falls today
     *
     * @param localDate date to check
     * @return true if it is today
     */
    public boolean checkIfToday(java.time.LocalDate localDate) {
        return java.time.LocalDate.now(timezone.toZoneId()).isEqual(localDate);
    }

    private ImmutableSet<String> getCountryHolidays(String countryCode) {
        ImmutableSet.Builder<String> holidayBuilder = ImmutableSet.builder();
        ImmutableSet.Builder<LocalDate> holidayLocalDateBuilder = ImmutableSet.builder();

        LocalDate localDate = LocalDate.fromDateFields(getNow());

        int year = localDate.getYear() - 20;

        HolidayCalendar calendar = HolidayCalendar.SWEDEN;
        if (countryCode.equals(COUNTRY_CODE_BELGIUM)) {
            calendar = HolidayCalendar.BELGIUM;
        } else if (countryCode.equals(COUNTRY_CODE_GB)) {
            calendar = HolidayCalendar.UNITED_KINGDOM;
        } else if (countryCode.equals(COUNTRY_CODE_PORTUGAL)) {
            calendar = HolidayCalendar.PORTUGAL;
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

        // Add New Years Eve
        if (holiday.getPropertiesKey().equals("NEW_YEAR")) {
            holidayLocalDateBuilder.add(holiday.getDate().minusDays(1));
        }
    }

    private Date getBestPossibleTransferDate(int cutOffHours, int cutOffMinutes) {
        Calendar calendar = getCalendar();
        calendar = moveToNextDayIfAfterCutOffTime(calendar, cutOffHours, cutOffMinutes);
        return getCurrentOrNextBusinessDay(calendar).getTime();
    }

    private Calendar moveToNextDayIfAfterCutOffTime(
            final Calendar calendar, int hours, int minutes) {
        Calendar businessDay = getCalendar(calendar.getTime());
        if (isAfterCutOffTime(businessDay, hours, minutes)) {
            businessDay.add(Calendar.DAY_OF_MONTH, 1);
        }
        return businessDay;
    }

    private boolean isAfterCutOffTime(Calendar calendar, int hour, int minute) {
        int currentHour = calendar.get(Calendar.HOUR_OF_DAY);
        int currentMinute = calendar.get(Calendar.MINUTE);
        return currentHour > hour || (currentHour == hour && currentMinute > minute);
    }
}
