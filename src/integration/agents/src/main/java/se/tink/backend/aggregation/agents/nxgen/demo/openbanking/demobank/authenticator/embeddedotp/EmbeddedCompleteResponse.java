package se.tink.backend.aggregation.agents.nxgen.demo.openbanking.demobank.authenticator.embeddedotp;

import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
public class EmbeddedCompleteResponse {
    private String accessToken;
    private String refreshToken;
    private Integer expiresIn;
    private Integer refreshExpiresIn;

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public Integer getExpiresIn() {
        return expiresIn;
    }

    public Integer getRefreshExpiresIn() {
        return refreshExpiresIn;
    }

    public OAuth2Token toOAuth2Token() {
        return OAuth2Token.create("bearer", accessToken, refreshToken, expiresIn, refreshExpiresIn);
    }
}
