package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.cbiglobe.CbiGlobeConstants.QueryKeys;

public class CbiUrlUtils {
    private static final String ENCODED_BLANK = "%20";

    @SneakyThrows
    public static String getEncondedRedirectURIQueryParams(String state, String result) {
        String sb = "?" + QueryKeys.STATE + "=" + state + "&" + QueryKeys.RESULT + "=" + result;
        return URLEncoder.encode(sb, StandardCharsets.UTF_8.toString());
    }

    public static String encodeBlankSpaces(String value) {
        return value.replace(StringUtils.SPACE, ENCODED_BLANK);
    }
}
