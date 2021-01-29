package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2;

import com.google.common.base.Strings;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Optional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStrongAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.CallbackParams;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.ErrorType;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OAuth2AuthenticationProgressiveController
        implements AutoAuthenticator, ThirdPartyAppStrongAuthenticator<String> {

    private static final Logger logger =
            LoggerFactory.getLogger(OAuth2AuthenticationProgressiveController.class);
    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final PersistentStorage persistentStorage;
    private final OAuth2Authenticator authenticator;
    private final Credentials credentials;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;

    private final String strongAuthenticationState;
    private final String strongAuthenticationStateSupplementalKey;

    public OAuth2AuthenticationProgressiveController(
            PersistentStorage persistentStorage,
            OAuth2Authenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState) {
        this(
                persistentStorage,
                authenticator,
                credentials,
                strongAuthenticationState,
                DEFAULT_TOKEN_LIFETIME,
                DEFAULT_TOKEN_LIFETIME_UNIT);
    }

    private OAuth2AuthenticationProgressiveController(
            PersistentStorage persistentStorage,
            OAuth2Authenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit) {
        this.persistentStorage = persistentStorage;
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

        if (oAuth2Token.hasAccessExpired()) {
            if (!oAuth2Token.canRefresh()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            // Refresh token is not always present, if it's absent we fall back to the manual
            // authentication.
            String refreshToken =
                    oAuth2Token
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

            OAuth2Token refreshedOAuth2Token = authenticator.refreshAccessToken(refreshToken);
            if (!refreshedOAuth2Token.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            if (refreshedOAuth2Token.hasRefreshExpire()) {
                credentials.setSessionExpiryDate(
                        OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                                refreshedOAuth2Token, tokenLifetime, tokenLifetimeUnit));
            }

            oAuth2Token = refreshedOAuth2Token.updateTokenWithOldToken(oAuth2Token);

            // Store the new access token on the persistent storage again.
            persistentStorage.rotateStorageValue(PersistentStorageKeys.OAUTH_2_TOKEN, oAuth2Token);

            // Fall through.
        }

        // Tell the authenticator which access token it can use.
        authenticator.useAccessToken(oAuth2Token);
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        URL authorizeUrl = authenticator.buildAuthorizeUrl(strongAuthenticationState);

        return ThirdPartyAppAuthenticationPayload.of(authorizeUrl);
    }

    @Override
    public final String getStrongAuthenticationStateSupplementalKey() {
        return strongAuthenticationStateSupplementalKey;
    }

    @Override
    public final long getWaitForMinutes() {
        return ThirdPartyAppConstants.WAIT_FOR_MINUTES;
    }

    @Override
    public ThirdPartyAppResponse<String> collect(final Map<String, String> callbackData)
            throws AuthenticationException, AuthorizationException {

        handleErrors(callbackData);

        String code = callbackData.getOrDefault(CallbackParams.CODE, null);
        if (Strings.isNullOrEmpty(code)) {
            throw new IllegalStateException("callbackData did not contain 'code'");
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
        authenticator.useAccessToken(oAuth2Token);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private Optional<String> getCallbackElement(Map<String, String> callbackData, String key) {
        String value = callbackData.getOrDefault(key, null);
        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private void handleErrors(Map<String, String> callbackData) throws AuthenticationException {
        Optional<String> error = getCallbackElement(callbackData, CallbackParams.ERROR);
        Optional<String> errorDescription =
                getCallbackElement(callbackData, OAuth2Constants.CallbackParams.ERROR_DESCRIPTION);

        if (!error.isPresent()) {
            logger.info("OAuth2 callback success.");
            return;
        }

        ErrorType errorType = ErrorType.getErrorType(error.get());
        if (ErrorType.ACCESS_DENIED.equals(errorType)
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
