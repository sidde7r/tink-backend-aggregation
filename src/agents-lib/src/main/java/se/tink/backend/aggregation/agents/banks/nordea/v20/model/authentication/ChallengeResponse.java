package se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallengeResponse {

    @JsonProperty("challengeResponse")
    private ChallengeResponseIn challengeResponseIn;

    public ChallengeResponseIn getChallengeResponseIn() {
        return challengeResponseIn;
    }

    public void setChallengeResponseIn(ChallengeResponseIn challengeResponseIn) {
        this.challengeResponseIn = challengeResponseIn;
    }

}
