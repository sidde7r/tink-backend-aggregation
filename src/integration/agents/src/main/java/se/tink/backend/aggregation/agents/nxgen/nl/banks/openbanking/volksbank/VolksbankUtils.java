package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class VolksbankUtils {

    private static final Logger log = LoggerFactory.getLogger(VolksbankUtils.class);

    private VolksbankUtils() {
        throw new AssertionError();
    }

    public static Map<String, String> splitURLQuery(String query) {
        if (query == null) {
            return null;
        }

        Map<String, String> query_pairs = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            query_pairs.put(parts[0], parts[1]);
        }
        return query_pairs;
    }

    public static boolean IsEntryReferenceFromAfterDate(String entryReferenceFrom, Date date) {
        final SimpleDateFormat entryReferenceDateFormat = new SimpleDateFormat("yyyyMMdd");
        try {
            return entryReferenceDateFormat
                            .parse(entryReferenceFrom.substring(0, 8))
                            .compareTo(date)
                    > 0;
        } catch (RuntimeException | ParseException e) {
            log.warn("Unable to parse entryReferenceFrom to date: " + entryReferenceFrom);
            return true;
        }
    }

    public static String getAccountNumber(String iban) {
        return iban.replace(" ", "").substring(8);
    }

    private static DateFormat getDateFormat() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df =
                new SimpleDateFormat(
                        "yyyy-MM-dd"); // Quoted "Z" to indicate UTC, no timezone offset
        df.setTimeZone(tz);
        return df;
    }
}
