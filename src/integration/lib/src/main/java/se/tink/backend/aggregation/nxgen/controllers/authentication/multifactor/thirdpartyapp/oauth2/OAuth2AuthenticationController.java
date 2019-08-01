package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.google.common.base.Strings;
import java.security.SecureRandom;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.configuration.CallbackJwtSignatureKeyPair;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Constants.ErrorType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.utils.OAuthUtils;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.credentials.service.CredentialsRequest;
import se.tink.libraries.cryptography.ECDSAUtils;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OAuth2AuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Random random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();
    private static final Logger logger =
            LoggerFactory.getLogger(OAuth2AuthenticationController.class);
    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OAuth2Authenticator authenticator;
    private final CallbackJwtSignatureKeyPair callbackJWTSignatureKeyPair;
    private final Credentials credentials;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;

    private final String state;
    private final String pseudoId;
    private final String callbackUri;

    // This wait time is for the whole user authentication. Different banks have different
    // cumbersome authentication flows.
    private static final long WAIT_FOR_MINUTES = 9;

    public OAuth2AuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            CallbackJwtSignatureKeyPair callbackJWTSignatureKeyPair,
            CredentialsRequest credentialsRequest) {
        this(
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                callbackJWTSignatureKeyPair,
                credentialsRequest,
                DEFAULT_TOKEN_LIFETIME,
                DEFAULT_TOKEN_LIFETIME_UNIT);
    }

    public OAuth2AuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            CallbackJwtSignatureKeyPair callbackJWTSignatureKeyPair,
            CredentialsRequest credentialsRequest,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit) {
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.callbackJWTSignatureKeyPair = callbackJWTSignatureKeyPair;
        this.credentials = credentialsRequest.getCredentials();
        this.tokenLifetime = tokenLifetime;
        this.tokenLifetimeUnit = tokenLifetimeUnit;
        this.callbackUri = credentialsRequest.getCallbackUri();

        this.pseudoId = RandomUtils.generateRandomHexEncoded(8);
        this.state = getJwtState(pseudoId, credentialsRequest.getAppUriId());
    }

    private String getJwtState(String pseudoId, String appUriId) {
        if (!callbackJWTSignatureKeyPair.isEnabled()) {
            logger.info("Callback JWT not enabled, using pseudoId as state. State: {}", pseudoId);
            return pseudoId;
        }
        JWTCreator.Builder jwtBuilder =
                JWT.create().withIssuedAt(new Date()).withClaim("id", pseudoId);

        if (!Strings.isNullOrEmpty(appUriId)) {
            // smaller claim to be safe on the length of the state
            jwtBuilder.withClaim("appUriId", appUriId);
        }

        String signedState =
                jwtBuilder.sign(
                        Algorithm.ECDSA256(
                                ECDSAUtils.getPublicKeyByPath(
                                        callbackJWTSignatureKeyPair.getPublicKeyPath()),
                                ECDSAUtils.getPrivateKeyByPath(
                                        callbackJWTSignatureKeyPair.getPrivateKeyPath())));

        logger.info("JWT state: {}", signedState);

        return signedState;
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
                                OAuthUtils.formatSupplementalKey(state),
                                WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(
                                LoginError.INCORRECT_CREDENTIALS
                                        ::exception); // todo: change this exception

        handleErrors(callbackData);

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

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        accessToken, tokenLifetime, tokenLifetimeUnit));

        persistentStorage.put(OAuth2Constants.PersistentStorageKeys.ACCESS_TOKEN, accessToken);

        // Tell the authenticator which access token it can use.
        authenticator.useAccessToken(accessToken);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private Optional<String> getCallbackElement(Map<String, String> callbackData, String key) {
        String value = callbackData.getOrDefault(key, null);
        if (com.google.common.base.Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private void handleErrors(Map<String, String> callbackData) throws AuthenticationException {
        Optional<String> error =
                getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR);
        Optional<String> errorDescription =
                getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR_DESCRIPTION);

        if (!error.isPresent()) {
            logger.info("OAuth2 callback success.");
            return;
        }

        ErrorType errorType = ErrorType.getErrorType(error.get());
        if (OAuth2Constants.ErrorType.ACCESS_DENIED.equals(errorType)
                || ErrorType.LOGIN_REQUIRED.equals(errorType)) {
            logger.info(
                    "OAuth2 {} callback: {}",
                    errorType.getValue(),
                    SerializationUtils.serializeToString(callbackData));
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        throw new IllegalStateException(
                String.format("Unknown error: %s:%s.", errorType, errorDescription.orElse("")));
    }
}
