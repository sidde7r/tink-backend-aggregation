package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RespondToChallengeRequest extends SpankkiRequest {
    private String authenticationId;
    private String challengeResponse;

    public RespondToChallengeRequest setAuthenticationId(String authenticationId) {
        this.authenticationId = authenticationId;
        return this;
    }

    public RespondToChallengeRequest setChallengeResponse(String challengeResponse) {
        this.challengeResponse = challengeResponse;
        return this;
    }
}
