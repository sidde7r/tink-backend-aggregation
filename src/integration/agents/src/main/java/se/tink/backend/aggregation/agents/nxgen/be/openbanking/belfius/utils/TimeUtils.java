package se.tink.backend.aggregation.agents.nxgen.be.openbanking.belfius.utils;

import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static Date get90DaysDate(Date toDate) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(toDate);
        calendar.add(Calendar.DATE, -89);
        return calendar.getTime();
    }
}
