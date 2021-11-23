package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import java.util.Base64;
import lombok.Value;

@Value
public class KnabAuthorizationCredentials {

    String clientId;

    String clientSecret;

    public String encoded() {
        return Base64.getEncoder().encodeToString(value().getBytes());
    }

    private String value() {
        return String.format("%s:%s", clientId, clientSecret);
    }
}
