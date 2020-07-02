package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import com.google.common.base.Strings;
import java.util.Map;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
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

    default void handleSpecificCallbackDataError(Map<String, String> callbackData)
            throws AuthenticationException {
        final String code = callbackData.getOrDefault(OAuth2Constants.CallbackParams.CODE, null);

        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalStateException("callbackData did not contain 'code'");
        }
    }
}
