package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeEntity {
    private List<String> challenges;
    private String token;

    public List<String> getChallenges() {
        return challenges;
    }

    public String getToken() {
        return token;
    }
}
