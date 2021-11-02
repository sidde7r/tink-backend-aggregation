package se.tink.libraries.jollyday.holidays;

import de.jollyday.config.ChristianHoliday;
import de.jollyday.config.ChristianHolidayType;
import de.jollyday.config.Configuration;
import de.jollyday.config.Fixed;
import de.jollyday.config.FixedWeekdayBetweenFixed;
import de.jollyday.config.FixedWeekdayInMonth;
import de.jollyday.config.HolidayType;
import de.jollyday.config.Holidays;
import de.jollyday.config.Month;
import de.jollyday.config.MovingCondition;
import de.jollyday.config.Weekday;
import de.jollyday.config.Which;
import de.jollyday.config.With;
import java.util.List;

public abstract class HolidayConfigurationCreator {

    protected static final String BANK_HOLIDAY = "BANK_HOLIDAY";
    protected static final String NEW_YEAR = "NEW_YEAR";

    protected Configuration createEmptyConfiguration(String hierarchy, String description) {
        Configuration configuration = new Configuration();
        configuration.setHierarchy(hierarchy);
        configuration.setDescription(description);
        configuration.setHolidays(new Holidays());
        return configuration;
    }

    protected Fixed createFixed(Month month, Integer day, String description) {
        Fixed fixed = new Fixed();
        fixed.setDay(day);
        fixed.setDescriptionPropertiesKey(description);
        fixed.setMonth(month);
        return fixed;
    }

    protected Fixed createFixed(Month month, Integer day, String description, HolidayType type) {
        Fixed fixed = new Fixed();
        fixed.setDay(day);
        fixed.setDescriptionPropertiesKey(description);
        fixed.setMonth(month);
        fixed.setLocalizedType(type);
        return fixed;
    }

    protected Fixed createFixed(
            Month month,
            Integer day,
            String description,
            List<MovingCondition> movingConditionList) {
        Fixed fixed = new Fixed();
        fixed.setDay(day);
        fixed.setDescriptionPropertiesKey(description);
        fixed.setMonth(month);
        fixed.getMovingCondition().addAll(movingConditionList);
        return fixed;
    }

    protected FixedWeekdayInMonth createFixedWeekdayInMonth(
            Which which, Weekday weekday, Month month, String description) {
        FixedWeekdayInMonth fixedWeekdayInMonth = new FixedWeekdayInMonth();
        fixedWeekdayInMonth.setWhich(which);
        fixedWeekdayInMonth.setWeekday(weekday);
        fixedWeekdayInMonth.setMonth(month);
        fixedWeekdayInMonth.setDescriptionPropertiesKey(description);
        return fixedWeekdayInMonth;
    }

    protected ChristianHoliday createChristianHoliday(
            ChristianHolidayType type, HolidayType holidayType) {
        ChristianHoliday christianHoliday = new ChristianHoliday();
        christianHoliday.setType(type);
        christianHoliday.setLocalizedType(holidayType);
        return christianHoliday;
    }

    protected ChristianHoliday createChristianHoliday(ChristianHolidayType type) {
        ChristianHoliday christianHoliday = new ChristianHoliday();
        christianHoliday.setType(type);
        return christianHoliday;
    }

    protected FixedWeekdayBetweenFixed createFixedWeekdayBetweenFixed(
            Weekday weekday, String description, Fixed from, Fixed to) {
        FixedWeekdayBetweenFixed fixedWeekdayBetweenFixed = new FixedWeekdayBetweenFixed();
        fixedWeekdayBetweenFixed.setWeekday(weekday);
        fixedWeekdayBetweenFixed.setDescriptionPropertiesKey(description);
        fixedWeekdayBetweenFixed.setFrom(from);
        fixedWeekdayBetweenFixed.setTo(to);
        return fixedWeekdayBetweenFixed;
    }

    protected MovingCondition createMovingCondition(
            Weekday substitute, With with, Weekday weekday) {
        MovingCondition condition = new MovingCondition();
        condition.setWeekday(weekday);
        condition.setWith(with);
        condition.setSubstitute(substitute);
        return condition;
    }

    public abstract Configuration create();
}
