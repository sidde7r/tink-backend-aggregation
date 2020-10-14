package se.tink.backend.aggregation.agents.nxgen.be.banks.ing.authenticator.entities;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import se.tink.backend.aggregation.annotations.JsonObject;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonObject
public class AuthenticateTokenResultEntity {

    private PersonEntity person;

    private List<AuthenticateAccessTokenEntity> accessTokens;

    private AuthenticationEntity authentication;

    public String findAccessToken() {
        return accessTokens.stream()
                .findAny()
                .map(AuthenticateAccessTokenEntity::getAccessToken)
                .orElseThrow(() -> new IllegalStateException("No access token acquired"));
    }

    public String findRefreshToken() {
        return accessTokens.stream()
                .findAny()
                .map(AuthenticateAccessTokenEntity::getRefreshToken)
                .orElseThrow(() -> new IllegalStateException("No refresh token acquired"));
    }
}
