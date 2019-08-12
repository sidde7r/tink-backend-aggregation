package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.swedbank.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityTokenChallengeRequest {
    public SecurityTokenChallengeRequest(String response) {
        this.response = response;
    }

    private String response;

    public static SecurityTokenChallengeRequest createFromChallenge(String challenge) {
        return new SecurityTokenChallengeRequest(challenge);
    }
}
