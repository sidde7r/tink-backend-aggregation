package se.tink.backend.aggregation.agents.nxgen.se.openbanking.sbab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class AuthorizationCodeResponse {

    @JsonProperty("pending_authorization_code")
    private String pendingAuthorizationCode;

    @JsonProperty("autostart_token")
    private String autoStartToken;

    @JsonIgnore
    public String getPendingAuthorizationCode() {
        return pendingAuthorizationCode;
    }

    @JsonIgnore
    public String getAutoStartToken() {
        return autoStartToken;
    }
}
