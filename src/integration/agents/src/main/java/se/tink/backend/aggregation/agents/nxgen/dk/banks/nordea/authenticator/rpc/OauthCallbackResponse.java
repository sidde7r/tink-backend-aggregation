package se.tink.backend.aggregation.agents.nxgen.dk.banks.nordea.authenticator.rpc;

import java.util.Optional;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class OauthCallbackResponse {

    private String accessToken;
    private int expiresIn;
    private String idToken;
    private String refreshToken;
    private String scope;
    private String tokenType;

    public String getAccessToken() {
        return accessToken;
    }

    public int getExpiresIn() {
        return expiresIn;
    }

    public String getIdToken() {
        return idToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getScope() {
        return scope;
    }

    public String getTokenType() {
        return tokenType;
    }

    public Optional<OAuth2Token> toOauthToken() {
        if (accessToken == null || refreshToken == null || expiresIn == 0) {
            return Optional.empty();
        }
        return Optional.of(OAuth2Token.createBearer(accessToken, refreshToken, expiresIn));
    }
}
