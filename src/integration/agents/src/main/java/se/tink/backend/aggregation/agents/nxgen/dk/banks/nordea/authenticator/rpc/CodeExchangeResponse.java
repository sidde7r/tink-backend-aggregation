package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeExchangeResponse {
    private String code;

    public String getCode() {
        return code;
    }
}
