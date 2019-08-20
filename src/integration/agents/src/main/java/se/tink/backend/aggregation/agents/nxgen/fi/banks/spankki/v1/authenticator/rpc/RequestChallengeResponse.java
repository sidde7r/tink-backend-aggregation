package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.v1.rpc.SpankkiResponse;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestChallengeResponse extends SpankkiResponse {
    private String challenge;
    private String authenticationId;

    public String getChallenge() {
        return challenge;
    }

    public String getAuthenticationId() {
        return authenticationId;
    }
}
