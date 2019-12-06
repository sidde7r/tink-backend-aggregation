package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;

public class BankIdResponse {

    @JsonProperty("pending_authorization_code")
    private String authorizationCode;

    @JsonProperty("autostart_token")
    private String autostartToken;

    public String getAuthorizationCode() {
        return authorizationCode;
    }

    public String getAutostartToken() {
        return autostartToken;
    }
}
