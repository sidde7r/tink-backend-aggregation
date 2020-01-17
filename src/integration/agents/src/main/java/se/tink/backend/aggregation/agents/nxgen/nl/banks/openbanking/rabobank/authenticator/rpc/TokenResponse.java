package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.authenticator.rpc;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.rabobank.RabobankConstants.StorageKey;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@JsonObject
public class TokenResponse {

    private static final Logger logger = LoggerFactory.getLogger(TokenResponse.class);

    @JsonProperty("access_token")
    private String accessToken;

    @JsonProperty("expires_in")
    private String expiresIn;

    @JsonProperty("token_type")
    private String tokenType;

    private String state;

    private String scope;

    @JsonProperty("refresh_token")
    private String refreshToken;

    @JsonProperty("refresh_token_expires_in")
    private String refreshTokenExpiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public long getExpiresIn() {
        return Long.parseLong(expiresIn);
    }

    public String getTokenType() {
        return tokenType;
    }

    public String getState() {
        return state;
    }

    public String getScope() {
        return scope;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public long getRefreshTokenExpiresIn() {
        return Long.parseLong(refreshTokenExpiresIn);
    }

    public OAuth2Token toOauthToken(PersistentStorage persistentStorage) {

        logger.info("Got new refresh token " + refreshToken);
        OAuth2Token token =
                OAuth2Token.create(
                        getTokenType(),
                        getAccessToken(),
                        getRefreshToken(),
                        getExpiresIn(),
                        getRefreshTokenExpiresIn());
        persistentStorage.put(StorageKey.OAUTH_TOKEN, token);
        return token;
    }
}
