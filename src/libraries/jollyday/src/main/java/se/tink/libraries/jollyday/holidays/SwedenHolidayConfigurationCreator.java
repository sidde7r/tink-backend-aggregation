package se.tink.libraries.jollyday.holidays;

import de.jollyday.config.ChristianHolidayType;
import de.jollyday.config.Configuration;
import de.jollyday.config.Holidays;
import de.jollyday.config.Month;
import de.jollyday.config.Weekday;
import java.util.Arrays;

public class SwedenHolidayConfigurationCreator extends HolidayConfigurationCreator {

    @Override
    public Configuration create() {
        Configuration configuration = new Configuration();
        Holidays holidays = new Holidays();
        configuration.setHierarchy("se");
        configuration.setDescription("Sweden");
        configuration.setHolidays(holidays);
        holidays.getFixed()
                .addAll(
                        Arrays.asList(
                                createFixed(Month.JANUARY, 1, NEW_YEAR),
                                createFixed(Month.JANUARY, 6, "EPIPHANY"),
                                createFixed(Month.MAY, 1, "LABOUR_DAY"),
                                createFixed(Month.JUNE, 6, "NATIONAL_DAY"),
                                createFixed(Month.DECEMBER, 24, "CHRISTMAS_EVE"),
                                createFixed(Month.DECEMBER, 25, "CHRISTMAS"),
                                createFixed(Month.DECEMBER, 26, "BOXING_DAY"),
                                createFixed(Month.DECEMBER, 31, "NEW_YEARS_EVE")));
        holidays.getChristianHoliday()
                .addAll(
                        Arrays.asList(
                                createChristianHoliday(ChristianHolidayType.EASTER),
                                createChristianHoliday(ChristianHolidayType.GOOD_FRIDAY),
                                createChristianHoliday(ChristianHolidayType.EASTER_MONDAY),
                                createChristianHoliday(ChristianHolidayType.ASCENSION_DAY),
                                createChristianHoliday(ChristianHolidayType.PENTECOST)));
        holidays.getFixedWeekdayBetweenFixed()
                .addAll(
                        Arrays.asList(
                                createFixedWeekdayBetweenFixed(
                                        Weekday.FRIDAY,
                                        "MIDSUMMER_EVE",
                                        createFixed(Month.JUNE, 19, ""),
                                        createFixed(Month.JUNE, 25, "")),
                                createFixedWeekdayBetweenFixed(
                                        Weekday.SATURDAY,
                                        "MIDSUMMER",
                                        createFixed(Month.JUNE, 20, ""),
                                        createFixed(Month.JUNE, 26, "")),
                                createFixedWeekdayBetweenFixed(
                                        Weekday.SATURDAY,
                                        "ALL_SAINTS",
                                        createFixed(Month.OCTOBER, 31, ""),
                                        createFixed(Month.NOVEMBER, 6, ""))));
        return configuration;
    }
}
