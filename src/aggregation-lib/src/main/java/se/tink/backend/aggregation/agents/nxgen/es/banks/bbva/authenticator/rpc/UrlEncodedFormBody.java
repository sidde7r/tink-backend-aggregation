package se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.authenticator.rpc;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.es.banks.bbva.BbvaConstants;

// create a body for a post request of type url encoded form
public class UrlEncodedFormBody extends LinkedHashMap<String, String> {
    private static final String JOINING_DELIMITER = "&";
    private static final String NAME_VALUE_FORMAT = "%s=%s";

    // create login request body and return as url encoded formatted string
    public static String createLoginRequest(String username, String password) {
        return new UrlEncodedFormBody()
                .add(BbvaConstants.PostParameter.ORIGEN_KEY,
                        BbvaConstants.PostParameter.ORIGEN_VALUE)
                .add(BbvaConstants.PostParameter.EAI_TIPOCP_KEY,
                        BbvaConstants.PostParameter.EAI_TIPOCP_VALUE)
                .add(BbvaConstants.PostParameter.EAI_USER_KEY,
                        BbvaConstants.PostParameter.EAI_USER_VALUE_PREFIX + username)
                .add(BbvaConstants.PostParameter.EAI_PASSWORD_KEY,
                        password).getBodyValue();
    }

    public UrlEncodedFormBody add(String name, String value) {
        this.put(name, value);
        return this;
    }

    public String getBodyValue() {
        return this.entrySet().stream()
                .map(parameter -> getValuePair(parameter))
                .collect(Collectors.joining(JOINING_DELIMITER));
    }

    private String getValuePair(Map.Entry<String, String> parameter) {
        try {
            return String.format(NAME_VALUE_FORMAT, parameter.getKey(), URLEncoder.encode(parameter.getValue(),
                    BbvaConstants.Defaults.CHARSET));
        } catch(Exception e) {
            throw new IllegalStateException("Cannot create form body: " + e.getMessage());
        }
    }
}
