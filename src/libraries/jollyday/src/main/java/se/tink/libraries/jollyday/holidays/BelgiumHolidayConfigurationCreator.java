package se.tink.libraries.jollyday.holidays;

import static de.jollyday.config.HolidayType.OFFICIAL_HOLIDAY;
import static de.jollyday.config.HolidayType.UNOFFICIAL_HOLIDAY;

import de.jollyday.config.ChristianHolidayType;
import de.jollyday.config.Configuration;
import de.jollyday.config.Holidays;
import de.jollyday.config.Month;
import java.util.Arrays;

public class BelgiumHolidayConfigurationCreator extends HolidayConfigurationCreator {

    @Override
    public Configuration create() {
        Configuration configuration = new Configuration();
        Holidays holidays = new Holidays();
        configuration.setHierarchy("be");
        configuration.setDescription("Belgium");
        configuration.setHolidays(holidays);
        holidays.getFixed()
                .addAll(
                        Arrays.asList(
                                createFixed(Month.JANUARY, 1, NEW_YEAR, OFFICIAL_HOLIDAY),
                                createFixed(Month.MAY, 1, "LABOUR_DAY", OFFICIAL_HOLIDAY),
                                createFixed(Month.JULY, 21, "NATIONAL_DAY", OFFICIAL_HOLIDAY),
                                createFixed(Month.AUGUST, 15, "ASSUMPTION_DAY"),
                                createFixed(Month.NOVEMBER, 1, "ALL_SAINTS", OFFICIAL_HOLIDAY),
                                createFixed(Month.NOVEMBER, 2, "ALL_SOULS", UNOFFICIAL_HOLIDAY),
                                createFixed(Month.NOVEMBER, 11, "ARMISTICE", OFFICIAL_HOLIDAY),
                                createFixed(Month.NOVEMBER, 15, "KINGS_FEAST", UNOFFICIAL_HOLIDAY),
                                createFixed(Month.DECEMBER, 25, "CHRISTMAS", OFFICIAL_HOLIDAY),
                                createFixed(
                                        Month.DECEMBER, 31, "NEW_YEARS_EVE", UNOFFICIAL_HOLIDAY)));
        holidays.getChristianHoliday()
                .addAll(
                        Arrays.asList(
                                createChristianHoliday(
                                        ChristianHolidayType.EASTER, OFFICIAL_HOLIDAY),
                                createChristianHoliday(
                                        ChristianHolidayType.EASTER_MONDAY, OFFICIAL_HOLIDAY),
                                createChristianHoliday(
                                        ChristianHolidayType.ASCENSION_DAY, OFFICIAL_HOLIDAY),
                                createChristianHoliday(
                                        ChristianHolidayType.PENTECOST, OFFICIAL_HOLIDAY),
                                createChristianHoliday(
                                        ChristianHolidayType.PENTECOST_MONDAY, OFFICIAL_HOLIDAY)));
        return configuration;
    }
}
