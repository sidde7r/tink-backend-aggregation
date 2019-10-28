package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.Objects;
import org.apache.commons.lang3.StringUtils;

public class CbiGlobeUtils {
    private static final String ENCODED_BLANK = "%20";
    private static final int YEAR = 365;
    private static final int NINETY_DAYS = 90;

    private CbiGlobeUtils() {
        throw new AssertionError();
    }

    public static byte[] readFile(final String path) {
        try {
            return Files.readAllBytes(Paths.get(path));
        } catch (final IOException e) {
            throw new IllegalStateException(e.getMessage(), e);
        }
    }

    private static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
        return sdf.format(date);
    }

    public static String getCurrentDateFormatted() {
        return formatDate(new Date());
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static Date calculateFromDate(String isFirstManualDone) {
        return Objects.isNull(isFirstManualDone)
                ? parseLocal(LocalDate.now().minusDays(YEAR))
                : parseLocal(LocalDate.now().minusDays(NINETY_DAYS));
    }

    public static Date parseLocal(LocalDate localDate) {
        return Date.from(localDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
    }

    public static String encodeBlankSpaces(String value) {
        return value.replace(StringUtils.SPACE, ENCODED_BLANK);
    }
}
