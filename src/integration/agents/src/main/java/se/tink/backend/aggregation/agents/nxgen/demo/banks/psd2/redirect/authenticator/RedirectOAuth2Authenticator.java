package se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.authenticator;

import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_CANCEL_CASE_REGEX;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_CONFIGURABLE_SESSION_CASE_REGEX;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_FAILURE_CASE_REGEX;
import static se.tink.backend.aggregation.agents.nxgen.demo.banks.psd2.redirect.RedirectAuthenticationDemoAgentConstants.DEMO_PROVIDER_TEMPORARY_ERROR_CASE_REGEX;

import com.google.common.base.Strings;
import java.util.Base64;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.ThirdPartyAppException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;

public class RedirectOAuth2Authenticator implements OAuth2Authenticator {
    private static final String REDIRECT_HOST = "https://cdn.tink.se/fake-bank/redirect-v3.html";
    private static final String CODE = "1234";
    private static final Base64.Encoder BASE64_ENCODER = Base64.getEncoder();
    private static final long THIRTY_DAYS_IN_SECONDS = 2592000;
    private final String providerName;
    private final boolean redirectToOxfordPreprod;
    private final String preferredCallbackUri;
    private final Credentials credentials;

    public RedirectOAuth2Authenticator(
            boolean redirectToOxfordPreprod, String preferredCallbackUri, Credentials credentials) {
        this.redirectToOxfordPreprod = redirectToOxfordPreprod;
        this.preferredCallbackUri = preferredCallbackUri;
        this.credentials = credentials;
        this.providerName = credentials.getProviderName();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {

        URL authorizationUrl =
                new URL(REDIRECT_HOST).queryParam("code", CODE).queryParam("state", state);

        if (!Strings.isNullOrEmpty(preferredCallbackUri)) {
            authorizationUrl = authorizationUrl.queryParam("redirectUrl", preferredCallbackUri);
        } else {
            authorizationUrl =
                    authorizationUrl.queryParam("preprod", String.valueOf(redirectToOxfordPreprod));
        }
        return authorizationUrl;
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code)
            throws BankServiceException, ThirdPartyAppException {
        if (!CODE.equals(code)) {
            // Ensure the code we got back from the fake-bank is the one
            // we sent.
            throw SessionError.CONSENT_REVOKED.exception("No code present.");
        }

        if (providerName.matches(DEMO_PROVIDER_FAILURE_CASE_REGEX)) {
            throw ThirdPartyAppError.AUTHENTICATION_ERROR.exception();
        } else if (providerName.matches(DEMO_PROVIDER_CANCEL_CASE_REGEX)) {
            throw ThirdPartyAppError.CANCELLED.exception();
        } else if (providerName.matches(DEMO_PROVIDER_TEMPORARY_ERROR_CASE_REGEX)) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }

        long accessExpiresInSeconds = THIRTY_DAYS_IN_SECONDS;
        if (providerName.matches(DEMO_PROVIDER_CONFIGURABLE_SESSION_CASE_REGEX)) {
            accessExpiresInSeconds =
                    Integer.parseInt(credentials.getField(Field.Key.SESSION_EXPIRY_TIME));
        }
        long refreshExpiresInSeconds = accessExpiresInSeconds;

        String accessToken = BASE64_ENCODER.encodeToString("fakeAccessToken".getBytes());
        String refreshToken = BASE64_ENCODER.encodeToString("fakeRefreshToken".getBytes());

        return OAuth2Token.create(
                "bearer",
                accessToken,
                refreshToken,
                accessExpiresInSeconds,
                refreshExpiresInSeconds);
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken)
            throws SessionException, BankServiceException {
        throw SessionError.SESSION_EXPIRED.exception();
    }

    @Override
    public void useAccessToken(OAuth2Token accessToken) {}
}
