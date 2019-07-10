package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public final class Utils {

    private Utils() {
        throw new AssertionError("Suppress default constructor for noninstantiability");
    }

    public static String formatDateTime(Date date, String timestampFormat, String timezone) {
        TimeZone timeZone = TimeZone.getTimeZone(timezone);
        DateFormat dateFormat = new SimpleDateFormat(timestampFormat);
        dateFormat.setTimeZone(timeZone);
        return dateFormat.format(date);
    }

    public static String readFile(final String path) {
        try {
            return new String(Files.readAllBytes(Paths.get(path)));
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }
}
