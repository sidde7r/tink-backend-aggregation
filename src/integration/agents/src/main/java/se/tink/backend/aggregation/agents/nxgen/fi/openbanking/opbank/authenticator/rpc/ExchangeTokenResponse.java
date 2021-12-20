package se.tink.backend.aggregation.agents.nxgen.fi.openbanking.opbank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@Getter
public class ExchangeTokenResponse {

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("scope")
    private String scope;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("status")
    private String status;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonIgnore
    public OAuth2Token toOauth2Token() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }
}
