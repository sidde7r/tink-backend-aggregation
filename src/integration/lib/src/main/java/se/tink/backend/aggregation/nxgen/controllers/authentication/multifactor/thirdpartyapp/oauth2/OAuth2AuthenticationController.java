package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import com.google.common.base.Strings;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.ErrorType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OAuth2AuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(OAuth2AuthenticationController.class);
    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OAuth2Authenticator authenticator;
    private final Credentials credentials;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;

    private final String strongAuthenticationState;
    private final String strongAuthenticationStateSupplementalKey;

    public OAuth2AuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {
        this(
                persistentStorage,
                supplementalInformationHelper,
                authenticator,
                credentials,
                strongAuthenticationState,
                DEFAULT_TOKEN_LIFETIME,
                DEFAULT_TOKEN_LIFETIME_UNIT);
    }

    public OAuth2AuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OAuth2Authenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit) {
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.authenticator = authenticator;
        this.credentials = credentials;
        this.tokenLifetime = tokenLifetime;
        this.tokenLifetimeUnit = tokenLifetimeUnit;

        this.strongAuthenticationStateSupplementalKey =
                strongAuthenticationState.getSupplementalKey();
        this.strongAuthenticationState = strongAuthenticationState.getState();
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);
        logger.info(
                "[forceAuthenticate] OAuth2AuthenticationController.autoAuthenticate token exists for credentials: {}",
                credentials.getId());
        if (oAuth2Token.hasAccessExpired()) {
            if (!oAuth2Token.canRefresh()) {
                invalidateToken();
                throw SessionError.SESSION_EXPIRED.exception();
            }

            try {
                // Store the new access token on the persistent storage again.
                oAuth2Token = refreshToken(oAuth2Token);
                persistentStorage.rotateStorageValue(
                        PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);
            } catch (SessionException ex) {
                invalidateToken();
                throw ex;
            }
        }

        // Tell the authenticator which access token it can use.
        useAccessToken(oAuth2Token);
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl = authenticator.buildAuthorizeUrl(strongAuthenticationState);

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

        ThirdPartyAppAuthenticationPayload.Desktop desktop =
                new ThirdPartyAppAuthenticationPayload.Desktop();
        desktop.setUrl(authorizeUrl.get());
        payload.setDesktop(desktop);

        return payload;
    }

    protected Map<String, String> getCallbackData() throws AuthenticationException {
        return supplementalInformationHelper
                .waitForSupplementalInformation(
                        strongAuthenticationStateSupplementalKey,
                        ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                        TimeUnit.MINUTES)
                .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {

        Map<String, String> callbackData = getCallbackData();

        authenticator.handleSpecificCallbackDataError(callbackData);

        handleErrors(callbackData);

        String code = callbackData.getOrDefault(CallbackParams.CODE, null);
        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalStateException(
                    "callbackData did not contain 'code' and no error was handled");
        }

        OAuth2Token oAuth2Token = authenticator.exchangeAuthorizationCode(code);

        if (!oAuth2Token.isValid()) {
            throw new IllegalStateException("Invalid access token.");
        }

        if (!oAuth2Token.isBearer()) {
            throw new IllegalStateException(
                    String.format("Unknown token type '%s'.", oAuth2Token.getTokenType()));
        }

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        oAuth2Token, tokenLifetime, tokenLifetimeUnit));

        persistentStorage.put(PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);

        // Tell the authenticator which access token it can use.
        useAccessToken(oAuth2Token);

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
        Optional<String> error = getCallbackElement(callbackData, CallbackParams.ERROR);
        Optional<String> errorDescription =
                getCallbackElement(callbackData, CallbackParams.ERROR_DESCRIPTION);

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

    private OAuth2Token refreshToken(OAuth2Token oAuth2Token) throws SessionException {
        // Refresh token is not always present, if it's absent we fall back to the manual
        // authentication.
        String refreshToken =
                oAuth2Token.getRefreshToken().orElseThrow(SessionError.SESSION_EXPIRED::exception);
        try {
            OAuth2Token refreshedOAuth2Token = authenticator.refreshAccessToken(refreshToken);

            if (!refreshedOAuth2Token.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            if (refreshedOAuth2Token.hasRefreshExpire()) {
                credentials.setSessionExpiryDate(
                        OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                                refreshedOAuth2Token, tokenLifetime, tokenLifetimeUnit));
            }

            return refreshedOAuth2Token.updateTokenWithOldToken(oAuth2Token);
        } catch (HttpResponseException ex) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private void invalidateToken() {
        persistentStorage.remove(PersistentStorageKeys.OAUTH_2_TOKEN);
    }

    private void useAccessToken(OAuth2Token token) {
        logger.info(
                String.format(
                        "Use a token valid for %s seconds. (issued at: %s s.; lifetime: %s s.)",
                        token.getValidForSecondsTimeLeft(),
                        token.getIssuedAt(),
                        token.getExpiresInSeconds()));
        authenticator.useAccessToken(token);
    }
}
