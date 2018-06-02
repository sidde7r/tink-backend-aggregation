package se.tink.backend.common.statistics.factory;

import java.util.Date;

import java.util.function.Function;
import se.tink.backend.common.statistics.StatisticsGeneratorFunctions;
import se.tink.backend.common.statistics.functions.WeeklyPeriodizationFunction;
import se.tink.libraries.date.ResolutionTypes;

public class PeriodFunctionFactory {
    private WeeklyPeriodizationFunction weeklyPeriodizationFunction;

    public PeriodFunctionFactory(String locale) {
        this.weeklyPeriodizationFunction = new WeeklyPeriodizationFunction(locale);
    }

    public Function<Date, String> getPeriodFunction(ResolutionTypes resolution) {
        Function<Date, String> periodFunction;
        switch (resolution) {
        case YEARLY:
            periodFunction = StatisticsGeneratorFunctions.YEARLY_PERIODIZATION_FUNCTION;
            break;
        case DAILY:
            periodFunction = StatisticsGeneratorFunctions.DAILY_PERIODIZATION_FUNCTION;
            break;
        case WEEKLY:
            periodFunction = weeklyPeriodizationFunction;
            break;
        default:
            periodFunction = StatisticsGeneratorFunctions.MONTHLY_PERIODIZATION_FUNCTION;
            break;
        }
        return periodFunction;
    }
}
