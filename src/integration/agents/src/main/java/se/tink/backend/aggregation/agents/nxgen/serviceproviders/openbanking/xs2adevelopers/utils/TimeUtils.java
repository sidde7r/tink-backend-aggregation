package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.xs2adevelopers.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class TimeUtils {

    public static String getDate() {
        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(Calendar.DATE, 89);
        return dateFormat.format(calendar.getTime());
    }
}
