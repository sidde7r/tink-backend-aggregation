package se.tink.libraries.jollyday.holidays;

import de.jollyday.config.ChristianHolidayType;
import de.jollyday.config.Configuration;
import de.jollyday.config.Month;
import java.util.Arrays;

public class FranceHolidayConfigurationCreator extends HolidayConfigurationCreator {

    @Override
    public Configuration create() {
        Configuration configuration = createEmptyConfiguration("fr", "France");
        configuration
                .getHolidays()
                .getFixed()
                .addAll(
                        Arrays.asList(
                                createFixed(Month.JANUARY, 1, NEW_YEAR),
                                createFixed(Month.MAY, 1, "LABOUR_DAY"),
                                createFixed(Month.MAY, 8, "VICTORY_DAY"),
                                createFixed(Month.JULY, 14, "NATIONAL_DAY"),
                                createFixed(Month.AUGUST, 15, "ASSUMPTION_MARY"),
                                createFixed(Month.NOVEMBER, 1, "ALL_SAINTS"),
                                createFixed(Month.NOVEMBER, 11, "REMEMBRANCE"),
                                createFixed(Month.DECEMBER, 25, "CHRISTMAS")));
        configuration
                .getHolidays()
                .getChristianHoliday()
                .addAll(
                        Arrays.asList(
                                createChristianHoliday(ChristianHolidayType.EASTER),
                                createChristianHoliday(ChristianHolidayType.EASTER_MONDAY),
                                createChristianHoliday(ChristianHolidayType.ASCENSION_DAY),
                                createChristianHoliday(ChristianHolidayType.WHIT_MONDAY)));

        Configuration maConfig = createEmptyConfiguration("ma", "Martinique");
        maConfig.getHolidays().getFixed().add(createFixed(Month.MAY, 22, ""));
        Configuration guConfig = createEmptyConfiguration("gu", "Guadeloupe");
        guConfig.getHolidays().getFixed().add(createFixed(Month.MAY, 27, ""));
        Configuration gyConfig = createEmptyConfiguration("gy", "Guyane");
        gyConfig.getHolidays().getFixed().add(createFixed(Month.JUNE, 10, ""));
        Configuration lrConfig = createEmptyConfiguration("lr", "La Reunion");
        lrConfig.getHolidays().getFixed().add(createFixed(Month.DECEMBER, 20, ""));
        Configuration brConfig = createEmptyConfiguration("br", "Bas-Rhin");
        brConfig.getHolidays().getFixed().add(createFixed(Month.DECEMBER, 26, ""));
        brConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.GOOD_FRIDAY));
        Configuration hrConfig = createEmptyConfiguration("hr", "Haut-Rhin");
        hrConfig.getHolidays().getFixed().add(createFixed(Month.DECEMBER, 26, ""));
        hrConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.GOOD_FRIDAY));
        Configuration moConfig = createEmptyConfiguration("mo", "Moselle");
        moConfig.getHolidays().getFixed().add(createFixed(Month.DECEMBER, 26, ""));
        configuration
                .getSubConfigurations()
                .addAll(
                        Arrays.asList(
                                maConfig, guConfig, gyConfig, lrConfig, brConfig, hrConfig,
                                moConfig));
        return configuration;
    }
}
