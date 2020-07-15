package se.tink.backend.aggregation.agents.nxgen.se.banks.swedbank.serviceprovider.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SecurityTokenChallengeRequest {
    public SecurityTokenChallengeRequest(String response) {
        this.response = response;
    }

    private String response;

    public static SecurityTokenChallengeRequest createFromChallengeResponse(
            String challengeResponse) {
        return new SecurityTokenChallengeRequest(challengeResponse);
    }
}
