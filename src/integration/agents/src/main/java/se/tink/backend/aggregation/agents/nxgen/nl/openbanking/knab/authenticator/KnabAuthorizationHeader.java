package se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.authenticator;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.nxgen.nl.openbanking.knab.KnabAuthorizationCredentials;

@Getter
@RequiredArgsConstructor
public class KnabAuthorizationHeader {

    private final KnabAuthorizationCredentials authorizationCredentials;

    public String value() {
        return String.format("Basic %s", authorizationCredentials.encoded());
    }
}
