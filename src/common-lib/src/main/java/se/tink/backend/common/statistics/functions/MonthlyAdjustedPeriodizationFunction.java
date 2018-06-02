package se.tink.backend.common.statistics.functions;

import java.util.Date;
import java.util.function.Function;
import se.tink.libraries.date.ResolutionTypes;
import se.tink.libraries.date.DateUtils;

/**
 * Transaction periodization function (MONTHLY_ADJUSTED)
 * 
 */
public class MonthlyAdjustedPeriodizationFunction implements Function<Date, String> {
    private int periodAdjustedDay;

    public MonthlyAdjustedPeriodizationFunction(int periodAdjustedDay) {
        if (periodAdjustedDay == 0) {
            this.periodAdjustedDay = 25;
        } else {
            this.periodAdjustedDay = periodAdjustedDay;
        }
    }

    @Override
    public String apply(Date d) {
        return DateUtils.getMonthPeriod(d, ResolutionTypes.MONTHLY_ADJUSTED, periodAdjustedDay);
    }
}
