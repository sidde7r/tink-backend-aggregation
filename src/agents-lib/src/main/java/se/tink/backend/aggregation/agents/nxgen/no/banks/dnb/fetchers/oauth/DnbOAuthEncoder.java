package se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.fetchers.oauth;

import com.google.api.client.util.Preconditions;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;
import se.tink.backend.aggregation.agents.nxgen.no.banks.dnb.DnbConstants;

public class DnbOAuthEncoder {
    private static final Map<String, String> ENCODING_RULES;

    static {
        HashMap<String, String> rules = new HashMap<>();
        rules.put("*", "%2A");
        rules.put("+", "%20");
        rules.put("%7E", "~");
        ENCODING_RULES = Collections.unmodifiableMap(rules);
    }

    public DnbOAuthEncoder() {
    }

    private static String applyRule(String target, String key, String value) {
        return target.replaceAll(Pattern.quote(key), value);
    }

    public static String encode(String target) {
        Preconditions.checkNotNull(target, "Cannot encode null object");

        try {
            target = URLEncoder.encode(target, DnbConstants.CHARSET);
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("Charset not found while encoding string: UTF-8", e);
        }

        for (Map.Entry<String, String> stringStringMap :  ENCODING_RULES.entrySet()) {
            target = applyRule(target, stringStringMap.getKey(), stringStringMap.getValue());
        }

        return target;
    }
}
