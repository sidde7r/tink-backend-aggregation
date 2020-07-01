package se.tink.backend.aggregation.agents.nxgen.fr.openbanking.bpcegroup.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@Getter
public class TokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("expires_in")
    private Long expiresIn;

    private String scope;

    @JsonProperty("token_type")
    private String tokenType;

    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }
}
