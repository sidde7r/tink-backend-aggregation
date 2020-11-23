package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
public class TokenResponse {

    private String tokenType;

    private String accessToken;

    private String refreshToken;

    private int expiresIn;

    private int refreshExpiresIn;

    private String idToken;

    public OAuth2Token toAccessToken() {
        return OAuth2Token.create(
                tokenType, accessToken, refreshToken, idToken, expiresIn, refreshExpiresIn);
    }
}
