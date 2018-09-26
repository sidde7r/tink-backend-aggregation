package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import com.google.common.base.Preconditions;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.assertj.core.util.Strings;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.common.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

// This authentication controller implements a basic execution flow around RFC 6749: The OAuth 2.0 Authorization Framework
// (https://tools.ietf.org/html/rfc6749).
// It does not perform any HTTP operations, the agent code that implements OAuth2Authenticator must perform the
// HTTP requests towards the URI this controller constructs.
public class OAuth2AuthenticationController implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Random random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();
    private final SupplementalInformationController supplementalInformationController;
    private final OAuth2Authenticator authenticator;
    private final OAuth2Authenticator.Configuration configuration;
    private final String redirectUri;
    private final String state;

    // This wait time is for the whole user authentication. Different banks have different cumbersome
    // authentication flows.
    private static final long WAIT_FOR_MINUTES = 10;

    public OAuth2AuthenticationController(SupplementalInformationController supplementalInformationController,
            OAuth2Authenticator authenticator, String redirectUri) {
        this.supplementalInformationController = supplementalInformationController;
        this.authenticator = Preconditions.checkNotNull(authenticator);
        this.configuration = Preconditions.checkNotNull(authenticator.getConfiguration());
        this.redirectUri = Preconditions.checkNotNull(redirectUri);
        this.state = generateRandomState();
    }

    private static String generateRandomState() {
        byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        // Request previously generated token from agent and verify it.

        OAuth2Token token = authenticator.loadToken()
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (!token.isValid()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) throws AuthenticationException,
            AuthorizationException {

        Map<String, String> callbackData = supplementalInformationController.waitForSupplementalInformation(
                this.state,
                WAIT_FOR_MINUTES,
                TimeUnit.MINUTES
        ).orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception); // todo: change this exception

        String code = callbackData.getOrDefault("code", null);
        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalStateException("callbackData did not contain 'code'");
        }

        URL accessTokenRequestUrl = buildAccessTokenRequestUrl(code);
        OAuth2TokenResponse response = authenticator.performAccessTokenRequest(accessTokenRequestUrl);

        OAuth2Token token = OAuth2Token.create(response);
        if (!token.isValid()) {
            return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.UNKNOWN);
        }

        // Let the agent store the token as it will be used by the agent.
        authenticator.storeToken(token);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizationUrl = buildAuthorizationUrl();

        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload = new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(authorizationUrl.get());
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload = new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(authorizationUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizationUrl.get());
        payload.setIos(iOsPayload);

        return payload;
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private URL buildAccessTokenRequestUrl(String code) {
        return configuration.getAccessTokenUrl()
                .queryParam("grant_type", configuration.getGrantType())
                .queryParam("code", code)
                .queryParam("redirect_uri", redirectUri)
                .queryParam("client_id", configuration.getClientId());
    }

    private URL buildAuthorizationUrl() {
        URL authorizationUrl = configuration.getAuthorizationUrl()
                .queryParam("response_type", configuration.getResponseType())
                .queryParam("client_id", configuration.getClientId())
                .queryParam("redirect_uri", redirectUri)
                .queryParam("state", state);

        Optional<String> scope = configuration.getScope();
        if (scope.isPresent()) {
            authorizationUrl = authorizationUrl.queryParam("scope", scope.get());
        }

        return authorizationUrl;
    }
}
