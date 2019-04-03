package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.utils;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.function.BiFunction;

public final class DateUtils {

    private DateUtils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static Date getRestrictedDate(Date date, Date restrictionDate,
        BiFunction<Date, Date, Boolean> dateEvaluator) {

        return dateEvaluator.apply(date, restrictionDate) ? date : restrictionDate;
    }

    public static String formatDateTime(Date date, String timestampFormat, String timezone) {
        TimeZone timeZone = TimeZone.getTimeZone(timezone);
        DateFormat dateFormat = new SimpleDateFormat(timestampFormat);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

}
