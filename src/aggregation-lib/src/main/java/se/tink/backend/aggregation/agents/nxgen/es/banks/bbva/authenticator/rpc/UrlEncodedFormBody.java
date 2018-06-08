package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;

public class UrlEncodedFormBody extends LinkedHashMap<String, String> {
    private static final String JOINING_DELIMITER = "&";
    private static final String NAME_VALUE_FORMAT = "%s=%s";

    public UrlEncodedFormBody add(String name, String value) {
        this.put(name, value);
        return this;
    }

    public String getBodyValue() {
        return this.entrySet().stream()
                .map(parameter -> {
                    try {
                        return String.format(NAME_VALUE_FORMAT, parameter.getKey(), URLEncoder.encode(parameter.getValue(),
                                BbvaConstants.Defaults.CHARSET));
                    } catch(Exception e) {
                        throw new IllegalStateException("Cannot create form body: " + e.getMessage());
                    }
                })
                .collect(Collectors.joining(JOINING_DELIMITER));
    }
}
