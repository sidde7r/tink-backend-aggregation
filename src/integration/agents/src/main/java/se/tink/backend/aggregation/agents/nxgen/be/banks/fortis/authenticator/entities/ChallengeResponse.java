package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeResponse {
    private ChallengeBusinessMessageBulk businessMessageBulk;
    private ChallengesValue value;

    public ChallengeBusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }

    public ChallengesValue getValue() {
        return value;
    }
}
