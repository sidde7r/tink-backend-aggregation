package se.tink.backend.aggregation.agents.nxgen.no.banks.nordea.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2TokenBase;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@Setter
@Getter
public class OauthTokenResponse {
    private String accessToken;
    private long expiresIn;
    private String idToken;
    private String refreshToken;
    private String scope;
    private String tokenType;
    private String uh;

    public Optional<OAuth2Token> toOauthToken() {
        if (accessToken == null || refreshToken == null || expiresIn == 0) {
            return Optional.empty();
        }

        return Optional.of(
                OAuth2Token.createBearer(
                        accessToken,
                        refreshToken,
                        null,
                        expiresIn,
                        OAuth2TokenBase.calculateExpiresInSeconds(refreshToken),
                        OAuth2TokenBase.extractIssuedAtSeconds(accessToken)));
    }
}
