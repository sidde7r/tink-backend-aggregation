package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.nordea.v30.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class FetchCodeRequest {
    private String code = "";

    public FetchCodeRequest setCode(String code) {
        this.code = code;
        return this;
    }
}
