package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.responses;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import lombok.Getter;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.authenticator.dto.common.ScopeDetailsEntity;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.polishapi.configuration.PolishApiConstants;
import se.tink.backend.aggregation.annotations.JsonObject;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;

@JsonObject
@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class TokenResponse {
    private String scope;

    private ScopeDetailsEntity scopeDetails;

    private String accessToken;

    private String refreshToken;

    private String expiresIn;

    private String tokenType;

    public OAuth2Token toOauthToken() {
        return OAuth2Token.create(
                PolishApiConstants.Authorization.BEARER,
                accessToken,
                refreshToken,
                Long.parseLong(expiresIn));
    }
}
