package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class CodeExchangeRequest {

    private String code;

    public CodeExchangeRequest(String code) {
        this.code = code;
    }
}
