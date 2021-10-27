package se.tink.libraries.jollyday.holidays;

import de.jollyday.config.ChristianHolidayType;
import de.jollyday.config.Configuration;
import de.jollyday.config.Fixed;
import de.jollyday.config.FixedWeekdayInMonth;
import de.jollyday.config.Month;
import de.jollyday.config.MovingCondition;
import de.jollyday.config.Weekday;
import de.jollyday.config.Which;
import de.jollyday.config.With;
import java.util.Arrays;

@SuppressWarnings("all")
public class UkHolidayConfigurationCreator extends HolidayConfigurationCreator {

    @Override
    public Configuration create() {
        Configuration configuration = createEmptyConfiguration("gb", "United Kingdom");

        MovingCondition satWithMon =
                createMovingCondition(Weekday.SATURDAY, With.NEXT, Weekday.MONDAY);
        MovingCondition sunWithTue =
                createMovingCondition(Weekday.SUNDAY, With.NEXT, Weekday.TUESDAY);
        MovingCondition monWithTue =
                createMovingCondition(Weekday.MONDAY, With.NEXT, Weekday.TUESDAY);

        MovingCondition sunWithMon =
                createMovingCondition(Weekday.SUNDAY, With.NEXT, Weekday.MONDAY);
        MovingCondition satWithTue =
                createMovingCondition(Weekday.SATURDAY, With.NEXT, Weekday.TUESDAY);

        Fixed boxingDay =
                createFixed(
                        Month.DECEMBER,
                        26,
                        "BOXING_DAY",
                        Arrays.asList(satWithMon, sunWithTue, monWithTue));
        Fixed christmas =
                createFixed(Month.DECEMBER, 25, "CHRISTMAS", Arrays.asList(satWithMon, sunWithMon));

        FixedWeekdayInMonth bankHoliday1 =
                createFixedWeekdayInMonth(Which.FIRST, Weekday.MONDAY, Month.MAY, BANK_HOLIDAY);
        FixedWeekdayInMonth bankHoliday2 =
                createFixedWeekdayInMonth(Which.LAST, Weekday.MONDAY, Month.MAY, BANK_HOLIDAY);

        configuration
                .getHolidays()
                .getChristianHoliday()
                .addAll(
                        Arrays.asList(
                                createChristianHoliday(ChristianHolidayType.EASTER),
                                createChristianHoliday(ChristianHolidayType.GOOD_FRIDAY)));

        Configuration alConfig = createEmptyConfiguration("al", "Alderney");
        alConfig.getHolidays().getFixed().add(createFixed(Month.JANUARY, 1, NEW_YEAR));
        alConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.EASTER_MONDAY));

        Configuration enConfig = createEmptyConfiguration("en", "England");
        enConfig.getHolidays()
                .getFixed()
                .add(
                        createFixed(
                                Month.JANUARY, 1, NEW_YEAR, Arrays.asList(satWithMon, sunWithMon)));
        enConfig.getHolidays()
                .getFixedWeekday()
                .add(
                        createFixedWeekdayInMonth(
                                Which.LAST, Weekday.MONDAY, Month.AUGUST, BANK_HOLIDAY));
        enConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.EASTER_MONDAY));

        Configuration guConfig = createEmptyConfiguration("gu", "Guernsey");
        guConfig.getHolidays().getFixed().add(createFixed(Month.JANUARY, 1, NEW_YEAR));
        guConfig.getHolidays().getFixed().add(createFixed(Month.MAY, 9, "LIBERATION"));
        guConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.EASTER_MONDAY));

        Configuration imConfig = createEmptyConfiguration("im", "Isle of Man");
        imConfig.getHolidays().getFixed().add(createFixed(Month.JANUARY, 1, NEW_YEAR));
        imConfig.getHolidays().getFixed().add(createFixed(Month.JULY, 5, "TYNWALD"));
        imConfig.getHolidays()
                .getFixedWeekday()
                .add(
                        createFixedWeekdayInMonth(
                                Which.SECOND, Weekday.FRIDAY, Month.JUNE, "TOURIST_TROPHY"));
        imConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.EASTER_MONDAY));

        Configuration jeConfig = createEmptyConfiguration("je", "Jersey");
        jeConfig.getHolidays().getFixed().add(createFixed(Month.JANUARY, 1, NEW_YEAR));
        jeConfig.getHolidays().getFixed().add(createFixed(Month.MAY, 9, "LIBERATION"));
        jeConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.EASTER_MONDAY));

        Configuration niConfig = createEmptyConfiguration("ni", "Northern Ireland");
        niConfig.getHolidays().getFixed().add(createFixed(Month.JANUARY, 1, NEW_YEAR));
        niConfig.getHolidays()
                .getFixed()
                .add(
                        createFixed(
                                Month.MARCH,
                                17,
                                "ST_PATRICK",
                                Arrays.asList(satWithMon, sunWithMon)));
        niConfig.getHolidays()
                .getFixed()
                .add(
                        createFixed(
                                Month.JULY,
                                12,
                                "BATTLE_BOYNE",
                                Arrays.asList(satWithMon, sunWithMon)));
        niConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.EASTER_MONDAY));

        Configuration scConfig = createEmptyConfiguration("sc", "Scotland");
        scConfig.getHolidays()
                .getFixed()
                .add(
                        createFixed(
                                Month.JANUARY, 1, NEW_YEAR, Arrays.asList(satWithTue, sunWithTue)));
        scConfig.getHolidays()
                .getFixed()
                .add(
                        createFixed(
                                Month.JANUARY, 2, NEW_YEAR, Arrays.asList(satWithMon, sunWithMon)));
        scConfig.getHolidays()
                .getFixedWeekday()
                .add(
                        createFixedWeekdayInMonth(
                                Which.FIRST, Weekday.MONDAY, Month.AUGUST, BANK_HOLIDAY));

        Configuration waConfig = createEmptyConfiguration("wa", "Wales");
        waConfig.getHolidays()
                .getFixed()
                .add(
                        createFixed(
                                Month.JANUARY, 1, NEW_YEAR, Arrays.asList(satWithMon, sunWithMon)));
        waConfig.getHolidays()
                .getFixedWeekday()
                .add(
                        createFixedWeekdayInMonth(
                                Which.FIRST, Weekday.MONDAY, Month.AUGUST, BANK_HOLIDAY));
        waConfig.getHolidays()
                .getChristianHoliday()
                .add(createChristianHoliday(ChristianHolidayType.EASTER_MONDAY));

        configuration.getHolidays().getFixed().addAll(Arrays.asList(boxingDay, christmas));
        configuration
                .getHolidays()
                .getFixedWeekday()
                .addAll(Arrays.asList(bankHoliday1, bankHoliday2));
        configuration
                .getSubConfigurations()
                .addAll(
                        Arrays.asList(
                                alConfig, enConfig, guConfig, imConfig, niConfig, scConfig,
                                waConfig));
        return configuration;
    }
}
