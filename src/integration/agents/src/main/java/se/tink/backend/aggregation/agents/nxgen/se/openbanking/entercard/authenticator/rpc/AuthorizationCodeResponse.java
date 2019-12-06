package se.tink.backend.aggregation.agents.nxgen.se.openbanking.entercard.authenticator.rpc;

import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationCodeResponse {

    private String code;

    public String getCode() {
        return code;
    }
}
