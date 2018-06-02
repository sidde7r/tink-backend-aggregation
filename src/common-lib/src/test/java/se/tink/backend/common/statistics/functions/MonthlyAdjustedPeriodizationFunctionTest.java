package se.tink.backend.common.statistics.functions;

import java.util.Calendar;
import org.junit.Ignore;
import org.junit.Test;

public class MonthlyAdjustedPeriodizationFunctionTest {

    /**
     * When running loop these are examples of dates throwing IllegalArgumentException
     * DateUtils{date=Tue Feb 24 00:00:00 CET 901, period=0901-03, firstDateInPeriod=Wed Feb 25 00:00:00 CET 901, lastDateInPeriod=Tue Mar 24 23:59:59 CET 901}
     * DateUtils{date=Mon Sep 24 00:00:00 CET 1582, period=1582-10, firstDateInPeriod=Tue Sep 25 00:00:00 CET 1582, lastDateInPeriod=Sun Oct 24 23:59:59 CET 1582}
     * <p>
     * To debug it, just add some logging in DateUtils where it currently throws IllegalArgument. It's hard to construct
     * the dates because the dates are invalid in some sense because of shaky type Date (I expect).
     */
    @Ignore
    @Test
    public void shouldNotThrow() {
        int profilePeriodAdjustedDay = 25;
        MonthlyAdjustedPeriodizationFunction periodFunction =
                new MonthlyAdjustedPeriodizationFunction(profilePeriodAdjustedDay);

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.YEAR, 0);
        calendar.set(Calendar.MONTH, 1);
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        while (calendar.get(Calendar.YEAR) < 3000) {
            periodFunction.apply(calendar.getTime());
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }
    }
}
