package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.authenticator.rpc;

import java.net.URLEncoder;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.stream.Collectors;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.creditagricole.CreditAgricoleConstants;

public abstract class AbstractForm extends LinkedHashMap<String, String> {

    private static final String JOINING_DELIMITER = "&";
    private static final String NAME_VALUE_FORMAT = "%s=%s";

    public String getBodyValue() {
        return this.entrySet().stream()
                .map(this::getValuePair)
                .collect(Collectors.joining(JOINING_DELIMITER));
    }

    protected String getValuePair(Map.Entry<String, String> parameter) {
        try {
            return String.format(NAME_VALUE_FORMAT, parameter.getKey(), URLEncoder.encode(parameter.getValue(),
                    CreditAgricoleConstants.Form.CHARSET));
        } catch(Exception e) {
            throw new IllegalStateException("Cannot create form body: " + e.getMessage());
        }
    }
}
