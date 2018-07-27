package se.tink.backend.aggregation.nxgen.http;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;

public abstract class AbstractForm extends LinkedHashMap<String, String> {

    private static final String JOINING_DELIMITER = "&";
    private static final String NAME_VALUE_FORMAT = "%s=%s";
    private static final String CHARSET = "UTF-8";

    public String getBodyValue() {
        return this.entrySet().stream()
                .map(this::getValuePair)
                .collect(Collectors.joining(JOINING_DELIMITER));
    }

    private String getValuePair(Map.Entry<String, String> parameter) {
        try {
            return String.format(NAME_VALUE_FORMAT,
                    URLEncoder.encode(parameter.getKey(), CHARSET),
                    URLEncoder.encode(parameter.getValue(), CHARSET));
        } catch(Exception e) {
            throw new IllegalStateException("Cannot create form body: " + e.getMessage());
        }
    }
}
