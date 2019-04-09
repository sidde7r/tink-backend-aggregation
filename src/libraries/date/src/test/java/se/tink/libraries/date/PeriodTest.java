package se.tink.libraries.date;

import static org.junit.Assert.assertEquals;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class PeriodTest {
    @Test
    @Parameters({
        "2017-03-26 10:10:09, 2017-03-26 10:10:20, 2017-03-26 10:10:10, true",
        "2017-03-26 10:10:10, 2017-03-26 10:10:20, 2017-03-26 10:10:10, true",
        "2017-03-26 10:10:11, 2017-03-26 10:10:20, 2017-03-26 10:10:10, false",
    })
    public void isDateWithinInclusive(
            String startDate, String endDate, String date, boolean isWithin) {
        se.tink.libraries.date.Period period = new se.tink.libraries.date.Period();
        period.setStartDate(DateUtils.parseDate(startDate));
        period.setEndDate(DateUtils.parseDate(endDate));

        assertEquals(isWithin, period.isDateWithinInclusive(DateUtils.parseDate(date)));
    }

    @Test
    @Parameters({
        "2017-03-26 10:10:10, 2017-03-27 10:10:10, 2017-03-26 10:11:10, 2017-03-26 14:11:10, true", // inside
        "2017-03-26 10:10:10, 2017-03-27 10:10:10, 2017-03-26 10:10:10, 2017-03-27 10:10:10, true", // exactly the same
        "2017-03-26 10:10:10, 2017-03-26 10:10:10, 2017-03-26 10:10:10, 2017-03-26 10:10:10, true", // one point
        "2017-03-26 10:10:10, 2017-03-27 10:10:10, 2017-03-26 10:11:10, 2017-03-28 14:11:10, false", // end date is out
    })
    public void isPeriodWithinInclusive(
            String startDate,
            String endDate,
            String newPeriodStartDate,
            String newPeriodEndDate,
            boolean isWithin) {
        Period period1 = new se.tink.libraries.date.Period();
        period1.setStartDate(DateUtils.parseDate(startDate));
        period1.setEndDate(DateUtils.parseDate(endDate));

        se.tink.libraries.date.Period period2 = new se.tink.libraries.date.Period();
        period2.setStartDate(DateUtils.parseDate(newPeriodStartDate));
        period2.setEndDate(DateUtils.parseDate(newPeriodEndDate));

        assertEquals(isWithin, period1.isPeriodWithinInclusive(period2));
    }
}
