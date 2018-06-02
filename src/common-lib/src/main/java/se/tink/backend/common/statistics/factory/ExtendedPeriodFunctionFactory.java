package se.tink.backend.common.statistics.factory;

import java.util.Date;
import java.util.function.Function;
import se.tink.backend.common.statistics.functions.MonthlyAdjustedPeriodizationFunction;
import se.tink.libraries.date.ResolutionTypes;

public class ExtendedPeriodFunctionFactory extends PeriodFunctionFactory {
    private final MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction;

    public ExtendedPeriodFunctionFactory(String locale,
      MonthlyAdjustedPeriodizationFunction monthlyAdjustedPeriodizationFunction) {
        super(locale);
        this.monthlyAdjustedPeriodizationFunction = monthlyAdjustedPeriodizationFunction;
    }

    @Override
    public Function<Date, String> getPeriodFunction(ResolutionTypes resolution) {
        switch (resolution) {
        case MONTHLY_ADJUSTED:
            return monthlyAdjustedPeriodizationFunction;
        default:
            return super.getPeriodFunction(resolution);
        }
    }
}
