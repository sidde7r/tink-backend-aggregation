package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeValue {
    private List<String> challenges;

    public List<String> getChallenges() {
        return challenges;
    }
}
