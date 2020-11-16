package se.tink.backend.aggregation.agents.nxgen.no.banks.nordeapoc.authenticator.rpc;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import java.util.Optional;
import lombok.Getter;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
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
        return Optional.of(OAuth2Token.createBearer(accessToken, refreshToken, expiresIn));
    }
}
