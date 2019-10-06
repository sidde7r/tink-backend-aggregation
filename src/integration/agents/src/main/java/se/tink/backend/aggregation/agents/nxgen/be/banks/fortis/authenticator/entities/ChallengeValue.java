package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeValue {
    private ChallengeEntity challenge;
    private String token;

    public ChallengeEntity getChallenge() {
        return challenge;
    }

    public String getToken() {
        return token;
    }
}
