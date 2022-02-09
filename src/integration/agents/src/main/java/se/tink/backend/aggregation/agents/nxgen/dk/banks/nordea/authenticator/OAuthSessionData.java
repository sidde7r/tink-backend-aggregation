package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class OAuthSessionData {

    private final String codeChallenge;
    private final String state;
    private final String nonce;
}
