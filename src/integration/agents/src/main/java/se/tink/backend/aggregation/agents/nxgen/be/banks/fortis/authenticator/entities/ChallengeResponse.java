package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeResponse {
    private ChallengeBusinessMessageBulk businessMessageBulk;
    private ChallengeValue value;

    public ChallengeBusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }

    public ChallengeValue getValue() {
        return value;
    }
}
