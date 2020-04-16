package se.tink.backend.aggregation.agents.nxgen.de.openbanking.dkb.authenticator;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Data;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@Data
@JsonObject
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class Wso2Token {
    private String accessToken;
    private String scope;
    private String tokenType;
    private Long expiresIn;

    OAuth2Token toOAuth2Token() {
        return OAuth2Token.create(tokenType, accessToken, null, expiresIn);
    }
}
