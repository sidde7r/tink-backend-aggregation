package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.utls;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;

public class CbiGlobeUtils {
    private static final String ENCODED_BLANK = "%20";

    private CbiGlobeUtils() {
        throw new AssertionError();
    }

    public static String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("EEE, dd MMM yyyy hh:mm:ss z", Locale.ENGLISH);
        return sdf.format(date);
    }

    @SneakyThrows
    public static String getEncondedRedirectURIQueryParams(
            String state, String consentType, String value) {
        StringBuilder sb = new StringBuilder();
        sb.append("?");
        sb.append(QueryKeys.STATE + "=" + state);
        sb.append("&");
        sb.append(QueryKeys.CODE + "=" + consentType);
        sb.append("&");
        sb.append(QueryKeys.RESULT + "=" + value);
        return URLEncoder.encode(sb.toString(), StandardCharsets.UTF_8.toString());
    }

    public static String encodeBlankSpaces(String value) {
        return value.replace(StringUtils.SPACE, ENCODED_BLANK);
    }
}
