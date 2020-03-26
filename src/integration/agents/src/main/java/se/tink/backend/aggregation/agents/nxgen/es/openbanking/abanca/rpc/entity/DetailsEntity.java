package se.tink.backend.aggregation.agents.nxgen.es.openbanking.abanca.rpc.entity;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class DetailsEntity {
    private String solutionHint;
    private String challengeId;
    private int attemptsLeft;
    private String type;
    private String responseMode;
    private int expiresIn;

    public String getSolutionHint() {
        return solutionHint;
    }

    public String getChallengeId() {
        return challengeId;
    }

    public int getAttemptsLeft() {
        return attemptsLeft;
    }

    public String getType() {
        return type;
    }

    public String getResponseMode() {
        return responseMode;
    }

    public int getExpiresIn() {
        return expiresIn;
    }
}
