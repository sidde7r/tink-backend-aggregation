package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.executor.entities;

import com.google.common.base.Preconditions;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeEntity {
    private String challenge;

    public String getChallenge() {
        return Preconditions.checkNotNull(challenge);
    }
}
