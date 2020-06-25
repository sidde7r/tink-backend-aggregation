package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.entities;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PendingAuthCodeResponseEntity {
    @JsonProperty("pending_authorization_code")
    private String pendingAuthorizationCode;

    @JsonProperty("autostart_token")
    private String autostartToken;

    public String getPendingAuthorizationCode() {
        return pendingAuthorizationCode;
    }

    public String getAutostartToken() {
        return autostartToken;
    }
}
