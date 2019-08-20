package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator;

import com.google.common.base.Strings;
import java.util.Base64;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class RedirectOAuth2Authenticator implements OAuth2Authenticator {
    private static final String REDIRECT_HOST = "https://cdn.tink.se/fake-bank/redirect.html";
    private static final String CODE = "1234";
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final long THIRTY_DAYS_IN_SECONDS = 2592000;

    private final boolean redirectToOxfordStaging;
    private final String preferredCallbackUri;

    public RedirectOAuth2Authenticator(
            boolean redirectToOxfordStaging, String preferredCallbackUri) {
        this.redirectToOxfordStaging = redirectToOxfordStaging;
        this.preferredCallbackUri = preferredCallbackUri;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        URL authorizationUrl =
                new URL(REDIRECT_HOST).queryParam("code", CODE).queryParam("state", state);

        if (!Strings.isNullOrEmpty(preferredCallbackUri)) {
            authorizationUrl = authorizationUrl.queryParam("redirectUrl", preferredCallbackUri);
        } else {
            authorizationUrl =
                    authorizationUrl.queryParam("staging", String.valueOf(redirectToOxfordStaging));
        }

        return authorizationUrl;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        String accessToken = BASE64_ENCODER.encodeToString("fakeAccessToken".getBytes());
        String refreshToken = BASE64_ENCODER.encodeToString("fakeRefreshToken".getBytes());

        return OAuth2Token.createBearer(accessToken, refreshToken, THIRTY_DAYS_IN_SECONDS);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        String accessToken = BASE64_ENCODER.encodeToString("fakeAccessToken".getBytes());

        return OAuth2Token.createBearer(accessToken, refreshToken, THIRTY_DAYS_IN_SECONDS);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
