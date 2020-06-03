package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.creditagricole.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@Data
public class TokenResponse {
    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private long expiresIn;

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("id_token")
    private String idToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("last_sca_date")
    private String lastScaDate;

    @JsonProperty("error")
    private String error;

    @JsonProperty("error_description")
    private String errorDescription;

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }

    public String getAccessToken() {
        return accessToken;
    }
}
