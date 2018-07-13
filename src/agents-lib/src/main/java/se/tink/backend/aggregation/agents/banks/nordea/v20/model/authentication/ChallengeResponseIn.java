package se.tink.backend.aggregation.agents.banks.nordea.v20.model.authentication;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ChallengeResponseIn {
    private Map<String, Object> challenge;

    public Map<String, Object> getChallenge() {
        return challenge;
    }

    public void setChallenge(Map<String, Object> challenge) {
        this.challenge = challenge;
    }

    private String getType() {
        return (String) challenge.get("$");
    }

    public String getId() {
        return (String) ((Map) challenge.get("@id")).get("$");
    }

    public String getChallengeCharacter() {
        return (String) challenge.get("$");
    }
}
