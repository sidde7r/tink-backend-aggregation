package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.n26.authenticator.rpc.app;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class MultiFactorAppResponse {
    private String challengeType;

    public String getChallengeType() {
        return challengeType;
    }
}
