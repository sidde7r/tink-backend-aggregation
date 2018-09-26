package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import java.util.Optional;

public interface OAuth2TokenResponse {
    String getTokenType();
    String getAccessToken();
    Optional<String> getRefreshToken();
    long getExpiresInSeconds();
}
