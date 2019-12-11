package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.commons.lang3.StringUtils;

public class CbiGlobeUtils {
    private static final String ENCODED_BLANK = "%20";

    private CbiGlobeUtils() {
        throw new AssertionError();
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z");
        return sdf.format(date);
    }

    public static String encodeValue(String value) {
        try {
            return URLEncoder.encode(value, StandardCharsets.UTF_8.toString());
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException(ex.getCause());
        }
    }

    public static String encodeBlankSpaces(String value) {
        return value.replace(StringUtils.SPACE, ENCODED_BLANK);
    }
}
