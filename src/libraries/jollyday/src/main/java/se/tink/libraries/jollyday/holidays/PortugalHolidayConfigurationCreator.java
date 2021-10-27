package se.tink.libraries.jollyday.holidays;

import de.jollyday.config.ChristianHolidayType;
import de.jollyday.config.Configuration;
import de.jollyday.config.Month;
import java.util.Arrays;

public class PortugalHolidayConfigurationCreator extends HolidayConfigurationCreator {

    @Override
    public Configuration create() {
        Configuration configuration = createEmptyConfiguration("pt", "Portugal");
        configuration
                .getHolidays()
                .getFixed()
                .addAll(
                        Arrays.asList(
                                createFixed(Month.JANUARY, 1, NEW_YEAR),
                                createFixed(Month.MAY, 1, "LABOUR_DAY"),
                                createFixed(Month.APRIL, 25, "FREEDOM_DEMOCRACY"),
                                createFixed(Month.JUNE, 10, "NATIONAL_DAY"),
                                createFixed(Month.AUGUST, 15, "ASSUMPTION_DAY"),
                                createFixed(Month.OCTOBER, 5, "REPUBLIC_DAY"),
                                createFixed(Month.NOVEMBER, 1, "ALL_SAINTS"),
                                createFixed(Month.DECEMBER, 1, "INDEPENDENCE_DAY"),
                                createFixed(Month.DECEMBER, 8, "IMMACULATE_CONCEPTION"),
                                createFixed(Month.DECEMBER, 25, "CHRISTMAS")));
        configuration
                .getHolidays()
                .getChristianHoliday()
                .addAll(
                        Arrays.asList(
                                createChristianHoliday(ChristianHolidayType.EASTER),
                                createChristianHoliday(ChristianHolidayType.CARNIVAL),
                                createChristianHoliday(ChristianHolidayType.GOOD_FRIDAY),
                                createChristianHoliday(ChristianHolidayType.CORPUS_CHRISTI)));
        return configuration;
    }
}
