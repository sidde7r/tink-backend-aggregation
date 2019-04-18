package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;

import java.security.SecureRandom;
import java.util.Base64;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class OAuth2AuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Random random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OAuth2Authenticator authenticator;

    private final String state;

    // This wait time is for the whole user authentication. Different banks have different
    // cumbersome
    // authentication flows.
    private static final long WAIT_FOR_MINUTES = 9;

    public OAuth2AuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator) {
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;

        this.state = generateRandomState();
    }

    private static String generateRandomState() {
        byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token accessToken =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (accessToken.hasAccessExpired()) {
            if (!accessToken.canRefresh()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            persistentStorage.remove(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN);

            // Refresh token is not always present, if it's absent we fall back to the manual
            // authentication.
            String refreshToken =
                    accessToken
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

            accessToken = authenticator.refreshAccessToken(refreshToken);
            if (!accessToken.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            // Store the new access token on the persistent storage again.
            persistentStorage.put(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, accessToken);

            // Fall through.
        }

        // Tell the authenticator which access token it can use.
        authenticator.useAccessToken(accessToken);
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl = authenticator.buildAuthorizeUrl(state);

        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload =
                new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(authorizeUrl.get());
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload =
                new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(authorizeUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());
        payload.setIos(iOsPayload);

        return payload;
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {

        Map<String, String> callbackData =
                supplementalInformationHelper
                        .waitForSupplementalInformation(
                                formatSupplementalKey(state), WAIT_FOR_MINUTES, TimeUnit.MINUTES)
                        .orElseThrow(
                                LoginError.INCORRECT_CREDENTIALS
                                        ::exception); // todo: change this exception

        String code = callbackData.getOrDefault(OAuth2Constants.CallbackParams.CODE, null);
        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalStateException("callbackData did not contain 'code'");
        }

        OAuth2Token accessToken = authenticator.exchangeAuthorizationCode(code);

        if (!accessToken.isValid()) {
            throw new IllegalStateException("Invalid access token.");
        }

        if (!accessToken.isBearer()) {
            throw new IllegalStateException(
                    String.format("Unknown token type '%s'.", accessToken.getTokenType()));
        }

        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, accessToken);

        // Tell the authenticator which access token it can use.
        authenticator.useAccessToken(accessToken);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private String formatSupplementalKey(String key) {
        // Ensure third party callback information does not collide with other Supplemental
        // Information by using a
        // prefix. This prefix is the same in MAIN.
        return String.format("tpcb_%s", key);
    }
}
