package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class DateUtils {

    private DateUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String formatDateTime(Date date, String timestampFormat, String timezone) {
        TimeZone timeZone = TimeZone.getTimeZone(timezone);
        DateFormat dateFormat = new SimpleDateFormat(timestampFormat);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }
}
