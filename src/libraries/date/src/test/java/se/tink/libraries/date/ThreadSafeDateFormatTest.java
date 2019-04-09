package se.tink.libraries.date;

import junitparams.JUnitParamsRunner;
import junitparams.Parameters;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JUnitParamsRunner.class)
public class ThreadSafeDateFormatTest {
    @Test
    @Parameters({
        "2017, false",
        "2017:10, false",
        "2017-01, true",
        "2017-13, false",
        "2017-01-01, false",
    })
    public void fitsMonthlyFormat(String date, boolean fitsFormat) { // yyyy-MM
        Assert.assertEquals(fitsFormat, ThreadSafeDateFormat.FORMATTER_MONTHLY.fitsFormat(date));
    }
}
