package se.tink.backend.aggregation.agents.banks.swedbank.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallengeResponse {
    protected boolean userOneTimePassword;
    protected String challenge;

    public boolean isUserOneTimePassword() {
        return userOneTimePassword;
    }

    public void setUserOneTimePassword(boolean userOneTimePassword) {
        this.userOneTimePassword = userOneTimePassword;
    }

    public String getChallenge() {
        return challenge;
    }

    public void setChallenge(String challenge) {
        this.challenge = challenge;
    }
}
