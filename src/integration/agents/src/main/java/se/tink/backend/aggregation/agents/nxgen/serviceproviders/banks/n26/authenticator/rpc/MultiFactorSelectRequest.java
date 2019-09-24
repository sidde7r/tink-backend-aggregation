package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MultiFactorSelectRequest {
    private String challengeType;
    private String mfaToken;

    public MultiFactorSelectRequest(String challengeType, String mfaToken) {
        this.challengeType = challengeType;
        this.mfaToken = mfaToken;
    }

    public String getChallengeType() {
        return challengeType;
    }

    public String getMfaToken() {
        return mfaToken;
    }
}
