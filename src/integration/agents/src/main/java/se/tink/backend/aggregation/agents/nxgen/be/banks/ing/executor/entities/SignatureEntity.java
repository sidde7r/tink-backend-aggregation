package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.entities;

import java.util.List;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SignatureEntity {
    private List<ChallengeEntity> challenges;

    public List<ChallengeEntity> getChallenges() {
        return challenges;
    }
}
