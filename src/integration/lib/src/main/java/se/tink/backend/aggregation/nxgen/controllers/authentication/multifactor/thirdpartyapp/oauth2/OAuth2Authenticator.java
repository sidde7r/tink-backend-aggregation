package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public interface OAuth2Authenticator {

    URL buildAuthorizeUrl(String state);

    OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, AuthenticationException;

    /**
     * If refreshing doesn't work you must throw a {@link SessionException} or {@link
     * BankServiceException}
     */
    OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException;

    /**
     * When the authentication is done this method will be invoked to let the agent authenticator
     * use the access token (e.g. when building requests or attached in a authentication filter).
     */
    void useAccessToken(OAuth2Token accessToken);

    /**
     * If the callback has non-standard parameters defining errors, throw an appropriate exception.
     * The default implementation does nothing, standard errors are handled separately.
     *
     * @param callbackData parameters to OAuth2 callback
     * @throws AuthenticationException
     */
    default void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {}
}
