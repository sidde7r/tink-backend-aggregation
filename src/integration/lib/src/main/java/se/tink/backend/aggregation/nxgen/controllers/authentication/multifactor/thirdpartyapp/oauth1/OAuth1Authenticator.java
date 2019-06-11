package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth1;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth1Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public interface OAuth1Authenticator {

    /**
     * First call in OAuth 1.0a flow. Obtaining temporary request token which contains oauth_token
     * used to build authorize url and oauth_secret used in later parts of the flow.
     */
    OAuth1Token getRequestToken(String state);

    /**
     * Saving temporary token in session. OAuth Secret value will be used while obtaining access
     * token.
     */
    void useTemporaryToken(OAuth1Token accessToken);

    /** Builds a URL with usage of temporary token returned by getRequestToken */
    URL buildAuthorizeUrl(String oauthToken);

    /**
     * Last call in OAuth 1.0a flow. Obtaining access token which should be used while fetching data
     */
    OAuth1Token getAccessToken(String oauthToken, String oauthVerifier) throws SessionException;
}
