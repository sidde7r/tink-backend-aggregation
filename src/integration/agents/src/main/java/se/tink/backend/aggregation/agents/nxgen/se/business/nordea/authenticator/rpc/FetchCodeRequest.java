package se.tink.backend.aggregation.agents.nxgen.se.business.nordea.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCodeRequest {
    public final String code;

    public FetchCodeRequest(String code) {
        this.code = code;
    }
}
