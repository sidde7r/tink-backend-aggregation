package se.tink.backend.aggregation.agents.banks.lansforsakringar.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TokenChallengeRequest {
    protected int originalChallenge;
    protected String hash;
    protected String challengePair;

    public TokenChallengeRequest(int originalChallenge, String hash, String challengePair) {
        this.originalChallenge = originalChallenge;
        this.hash = hash;
        this.challengePair = challengePair;
    }

    public int getOriginalChallenge() {
        return originalChallenge;
    }

    public void setOriginalChallange(int originalChallenge) {
        this.originalChallenge = originalChallenge;
    }

    public String getHash() {
        return hash;
    }

    public void setHash(String hash) {
        this.hash = hash;
    }

    public String getChallengePair() {
        return challengePair;
    }

    public void setChallengePair(String challengePair) {
        this.challengePair = challengePair;
    }
}
