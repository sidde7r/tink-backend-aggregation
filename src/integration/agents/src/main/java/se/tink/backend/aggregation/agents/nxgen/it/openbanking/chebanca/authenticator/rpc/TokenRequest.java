package se.tink.backend.aggregation.agents.nxgen.it.openbanking.chebanca.authenticator.rpc;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.*;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.annotations.JsonObject;

@JsonObject
public class TokenRequest {

    @JsonProperty("client_id")
    private String clientId;

    @JsonProperty("client_secret")
    private String clientSecret;

    @JsonProperty("code")
    @JsonInclude(NON_NULL)
    private String code;

    @JsonProperty("refresh_token")
    @JsonInclude(NON_NULL)
    private String refreshToken;

    @JsonProperty("grant_type")
    private String grantType;

    @JsonProperty("redirect_uri")
    @JsonInclude(NON_NULL)
    private String redirectUri;

    public TokenRequest(
            String clientId,
            String clientSecret,
            String code,
            String grantType,
            String redirectUri) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.code = code;
        this.grantType = grantType;
        this.redirectUri = redirectUri;
    }

    public TokenRequest(
            String clientId, String clientSecret, String refreshToken, String grantType) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.refreshToken = refreshToken;
        this.grantType = grantType;
    }
}
