package se.tink.backend.aggregation.agents.nxgen.se.banks.sbab.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class PendingAuthCodeResponse {
    @JsonProperty("pending_authorization_code")
    private String pendingAuthorizationCode;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    public String getPendingAuthorizationCode() {
        return pendingAuthorizationCode;
    }

    public String getRedirectUri() {
        return redirectUri;
    }
}
