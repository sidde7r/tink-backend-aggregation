package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator;

import lombok.Value;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabAuthorizationCredentials;

@Value
public class BasicAuthorizationHeader {

    KnabAuthorizationCredentials authorizationCredentials;

    public String value() {
        return String.format("Basic %s", authorizationCredentials.encoded());
    }
}
