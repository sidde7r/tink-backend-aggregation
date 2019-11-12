package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeRequest {
    @JsonProperty private String requestToken;
    @JsonProperty private String requestInfo;

    public ChallengeRequest(String requestToken, String requestInfo) {
        this.requestToken = requestToken;
        this.requestInfo = requestInfo;
    }
}
