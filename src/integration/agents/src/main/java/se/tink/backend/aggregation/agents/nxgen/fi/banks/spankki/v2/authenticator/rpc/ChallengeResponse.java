package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v2.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class ChallengeResponse extends SpankkiResponse {
    @JsonProperty private String challenge = "";
    @JsonProperty private String authenticationId = "";
    @JsonProperty private String requestToken = "";

    @JsonIgnore
    public String getChallenge() {
        return challenge;
    }

    @JsonIgnore
    public String getAuthenticationId() {
        return authenticationId;
    }
}
