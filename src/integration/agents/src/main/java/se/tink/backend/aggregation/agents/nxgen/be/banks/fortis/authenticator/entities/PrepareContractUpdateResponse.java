package se.tink.backend.aggregation.agents.nxgen.be.banks.fortis.authenticator.entities;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PrepareContractUpdateResponse {
    private ChallengeBusinessMessageBulk businessMessageBulk;
    private ChallengeValue value;

    public ChallengeValue getValue() {
        return value;
    }

    public ChallengeBusinessMessageBulk getBusinessMessageBulk() {
        return businessMessageBulk;
    }
}
