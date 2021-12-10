package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab;

import java.util.Base64;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class KnabAuthorizationCredentials {

    private final String clientId;

    private final String clientSecret;

    public String encoded() {
        return Base64.getEncoder().encodeToString(value().getBytes());
    }

    private String value() {
        return String.format("%s:%s", clientId, clientSecret);
    }
}
