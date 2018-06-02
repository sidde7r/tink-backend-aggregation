package se.tink.backend.aggregation.agents.banks.handelsbanken.v6.model;

public class InitNewProfileResponse extends AbstractResponse {
    private String snonce;
    private String challenge;
    private String challengeTp;

    public String getSnonce() {
        return snonce;
    }

    public void setSnonce(String snonce) {
        this.snonce = snonce;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }

    public String getChallengeTp() {
        return challengeTp;
    }

    public void setChallengeTp(String challengeTp) {
        this.challengeTp = challengeTp;
    }

}
