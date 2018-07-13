package se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.authenticator.rpc;

import se.tink.backend.aggregation.agents.nxgen.fi.banks.spankki.rpc.SpankkiRequest;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RequestChallengeRequest extends SpankkiRequest {
    private String requestInfo;

    public RequestChallengeRequest setRequestInfo(String requestInfo) {
        this.requestInfo = requestInfo;
        return this;
    }
}
