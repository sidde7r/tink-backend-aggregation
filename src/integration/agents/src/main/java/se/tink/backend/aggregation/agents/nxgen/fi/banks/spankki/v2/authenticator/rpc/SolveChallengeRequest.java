package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class SolveChallengeRequest {
    @JsonProperty private String challengeResponse;
    @JsonProperty private String authenticationId;

    public SolveChallengeRequest(String authenticationId, String challengeResponse) {
        this.challengeResponse = challengeResponse;
        this.authenticationId = authenticationId;
    }
}
