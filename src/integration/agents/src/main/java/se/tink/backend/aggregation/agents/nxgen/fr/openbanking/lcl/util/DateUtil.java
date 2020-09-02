package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.lcl.util;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {
    private DateUtil() {}

    private static final DateTimeFormatter DATE_TIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    public static String plusOneDayDate(String date) {
        return ZonedDateTime.parse(date).plusDays(1L).format(DATE_TIME_FORMATTER);
    }
}
