package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class ChallengeResponseRequest {
    private String challengeData;
    private String response;

    public String getChallengeData() {
        return challengeData;
    }

    public String getResponse() {
        return response;
    }

    public void setChallengeData(String challengeData) {
        this.challengeData = challengeData;
    }

    public void setResponse(String response) {
        this.response = response;
    }
}
