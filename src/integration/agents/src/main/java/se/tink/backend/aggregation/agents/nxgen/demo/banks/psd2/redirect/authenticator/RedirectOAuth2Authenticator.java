package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;

public class RedirectOAuth2Authenticator implements OAuth2Authenticator {
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();

    @Override
    public URL buildAuthorizeUrl(String state) {
        return new URL("").queryParam("state", state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        String accessToken = BASE64_ENCODER.encodeToString("fakeAccessToken".getBytes());
        String refreshToken = BASE64_ENCODER.encodeToString("fakeRefreshToken".getBytes());
        long accessExpiresInSeconds =
                LocalDate.now().until(LocalDate.now().plusDays(90), ChronoUnit.SECONDS);

        return OAuth2Token.createBearer(accessToken, refreshToken, accessExpiresInSeconds);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        String accessToken = BASE64_ENCODER.encodeToString("fakeAccessToken".getBytes());
        long accessExpiresInSeconds =
                LocalDate.now().until(LocalDate.now().plusDays(90), ChronoUnit.SECONDS);

        return OAuth2Token.createBearer(accessToken, refreshToken, accessExpiresInSeconds);
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
