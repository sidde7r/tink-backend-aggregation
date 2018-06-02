package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class ChallengeRequest {
    private String cnonce;

    public ChallengeRequest(String cnonce) {
        this.cnonce = cnonce;
    }

    public String getCnonce() {
        return cnonce;
    }

    public void setCnonce(String cnonce) {
        this.cnonce = cnonce;
    }

}
