package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.controller;

public class ChallengeExchangeValues {
    private String challenge;
    private String signingId;

    public ChallengeExchangeValues(String challenge, String signingId) {
        this.challenge = challenge;
        this.signingId = signingId;
    }

    public String getChallenge() {
        return challenge;
    }

    public String getSigningId() {
        return signingId;
    }
}
