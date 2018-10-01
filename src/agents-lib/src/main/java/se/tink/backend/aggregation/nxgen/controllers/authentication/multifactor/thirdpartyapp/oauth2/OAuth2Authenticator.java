package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface OAuth2Authenticator {
    URL buildAuthorizeUrl(String state);
    OAuth2Token exchangeAuthorizationCode(String code);
    OAuth2Token refreshAccessToken(String refreshToken);

    // When the authentication is done this method will be invoked to let the agent authenticator use the access
    // token (e.g. when building requests or attached in a authentication filter).
    void useAccessToken(OAuth2Token accessToken);
}
