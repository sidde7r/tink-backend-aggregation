package se.tink.backend.aggregation.agents.nxgen.dk.openbanking.sdc.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class RefreshAccessTokenRequest {

    @JsonProperty("grant_type")
    private String grantType;

    private String code;

    @JsonProperty("redirect_uri")
    private String redirectUri;

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    private String scope;

    public RefreshAccessTokenRequest(
            String grantType,
            String code,
            String redirectUri,
            String clientId,
            String clientSecret,
            String scope) {
        this.grantType = grantType;
        this.code = code;
        this.redirectUri = redirectUri;
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.scope = scope;
    }
}
