package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator;

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
    private static final long THIRTHY_DAYS_IN_SECONDS = 2592000;

    private final boolean redirectToOxfordStaging;

    public RedirectOAuth2Authenticator(boolean redirectToOxfordStaging) {
        this.redirectToOxfordStaging = redirectToOxfordStaging;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        return new URL(REDIRECT_HOST)
                .queryParam("staging", String.valueOf(redirectToOxfordStaging))
                .queryParam("code", CODE)
                .queryParam("state", state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        String accessToken = BASE64_ENCODER.encodeToString("fakeAccessToken".getBytes());
        String refreshToken = BASE64_ENCODER.encodeToString("fakeRefreshToken".getBytes());

        return OAuth2Token.createBearer(accessToken, refreshToken, THIRTHY_DAYS_IN_SECONDS);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        String accessToken = BASE64_ENCODER.encodeToString("fakeAccessToken".getBytes());

        return OAuth2Token.createBearer(accessToken, refreshToken, THIRTHY_DAYS_IN_SECONDS);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
