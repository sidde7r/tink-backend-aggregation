package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth1Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface OAuth1Authenticator {

    URL buildAuthorizeUrl(String oauthToken);

    OAuth1Token getRequestToken(String state);

    OAuth1Token getAccessToken(String oauthToken, String oauthVerifier) throws SessionException;

    void useAccessToken(OAuth1Token accessToken);
}
