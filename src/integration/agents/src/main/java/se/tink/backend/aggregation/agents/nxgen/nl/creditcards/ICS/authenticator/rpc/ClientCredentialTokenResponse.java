package se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import se.tink.backend.aggregation.agents.nxgen.nl.creditcards.ICS.authenticator.entities.TppInformationEntity;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

public class ClientCredentialTokenResponse {

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("tppInformation")
    private TppInformationEntity tppInformation;

    @JsonProperty("token_type")
    private String tokenType;

    @JsonProperty("expires_in")
    private int expiresIn;

    @JsonProperty("jti")
    private String jti;

    public String getAccessToken() {
        return accessToken;
    }

    public TppInformationEntity getTppInformation() {
        return tppInformation;
    }

    public String getTokenType() {
        return tokenType;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getJti() {
        return jti;
    }

    public OAuth2Token toTinkToken() {
        return OAuth2Token.create(tokenType, accessToken, refreshToken, expiresIn);
    }
}
