package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiResponse;
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
