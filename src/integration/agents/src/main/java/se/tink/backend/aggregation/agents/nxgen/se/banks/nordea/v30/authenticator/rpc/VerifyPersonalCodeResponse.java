package se.tink.backend.aggregation.agents.nxgen.se.banks.nordea.v30.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class VerifyPersonalCodeResponse {
    @JsonProperty("authorization_code")
    private String authorizationCode;

    public String getAuthorizationCode() {
        return authorizationCode;
    }
}
