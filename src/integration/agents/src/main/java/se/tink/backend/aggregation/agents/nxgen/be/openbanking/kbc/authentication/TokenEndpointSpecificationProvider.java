package se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.authentication;

import java.net.URI;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import javax.ws.rs.core.HttpHeaders;
import se.tink.backend.aggregation.agents.nxgen.be.openbanking.kbc.configuration.KbcConfiguration;

public class TokenEndpointSpecificationProvider {

    static Map<String, String> getClientSpecificHeaders(KbcConfiguration kbcConfiguration) {
        Map<String, String> headers = new HashMap<>();
        headers.put("PSU-IP-Address", kbcConfiguration.getPsuIpAddress());
        headers.put(
                HttpHeaders.AUTHORIZATION,
                String.format(
                        "Basic %s",
                        Base64.getEncoder()
                                .encodeToString(kbcConfiguration.getClientId().getBytes())));
        return headers;
    }

    static URI getAccessTokenEndpoint() {
        return URI.create("https://openapi.kbc-group.com/ASK/oauth/token/1");
    }
}
