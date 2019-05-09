package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.volksbank;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;
import se.tink.backend.aggregation.nxgen.http.URL;

public class VolksbankUtils {

    public static URL buildURL(String uri) {

        StringBuilder s = new StringBuilder();
        s.append(VolksbankConstants.Urls.SANDBOX_URL);
        s.append(uri);
        return new URL(s.toString());
    }

    public static byte[] readFile(String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    public static Map<String, String> splitURLQuery(String query) {

        if (query == null) return null;

        Map<String, String> query_pairs = new HashMap<>();
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            String[] parts = pair.split("=");
            query_pairs.put(parts[0], parts[1]);
        }
        return query_pairs;
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

    public static String getCurrentDateAsString() {
        return getDateFormat().format(new Date());
    }

    public static String getFutureDateAsString(int year) {
        Calendar date = Calendar.getInstance();
        date.setTime(new Date());
        date.add(Calendar.YEAR, year);
        Date later = date.getTime();
        return getDateFormat().format(later);
    }
}
