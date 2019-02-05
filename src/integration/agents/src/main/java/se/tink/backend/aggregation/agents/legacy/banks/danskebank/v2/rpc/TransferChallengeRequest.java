package se.tink.backend.aggregation.agents.banks.danskebank.v2.rpc;

public class TransferChallengeRequest {
    private String challengeData;
    private String response;

    public String getResponse() {
        return response;
    }

    public void setResponse(String response) {
        this.response = response;
    }

    public String getChallengeData() {
        return challengeData;
    }

    public void setChallengeData(String challengeData) {
        this.challengeData = challengeData;
    }

}
