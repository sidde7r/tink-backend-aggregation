package se.tink.backend.aggregation.agents.nxgen.be.openbanking.argenta.utils;

import java.util.Calendar;
import java.util.Date;

public final class TimeUtils {

    private TimeUtils() {
        throw new AssertionError();
    }

    public static Date get90DaysDate(Date toDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDate);
        calendar.add(Calendar.DATE, -89);
        return calendar.getTime();
    }
}
