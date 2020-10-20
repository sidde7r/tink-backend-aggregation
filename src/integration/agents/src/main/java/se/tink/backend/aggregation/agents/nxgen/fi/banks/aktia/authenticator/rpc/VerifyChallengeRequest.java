package se.tink.backend.aggregation.agents.nxgen.fi.banks.aktia.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class VerifyChallengeRequest {

    private final String codeChallenge;

    public VerifyChallengeRequest(String codeChallenge) {
        this.codeChallenge = codeChallenge;
    }

    public String getCodeChallenge() {
        return codeChallenge;
    }
}
