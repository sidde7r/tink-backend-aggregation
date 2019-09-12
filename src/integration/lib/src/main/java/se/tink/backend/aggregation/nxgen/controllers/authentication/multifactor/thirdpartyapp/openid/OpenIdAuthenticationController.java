package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.PersistentStorageKeys;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.OpenBankingTokenExpirationDateHelper;
import se.tink.backend.aggregation.nxgen.controllers.authentication.utils.StrongAuthenticationState;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationHelper;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpenIdAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private static final Logger logger =
            LoggerFactory.getLogger(OpenIdAuthenticationController.class);

    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OpenIdApiClient apiClient;
    private final OpenIdAuthenticator authenticator;
    private final Credentials credentials;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;

    private final String strongAuthenticationState;
    private final String strongAuthenticationStateSupplementalKey;

    private final String nonce;

    private final String callbackUri;
    private OAuth2Token clientAccessToken;
    private final URL appToAppRedirectURL;

    public OpenIdAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OpenIdApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            URL appToAppRedirectURL) {
        this(
                persistentStorage,
                supplementalInformationHelper,
                apiClient,
                authenticator,
                credentials,
                strongAuthenticationState,
                callbackUri,
                DEFAULT_TOKEN_LIFETIME,
                DEFAULT_TOKEN_LIFETIME_UNIT,
                appToAppRedirectURL);
    }

    public OpenIdAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OpenIdApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit,
            URL appToAppRedirectURL) {
        this.persistentStorage = persistentStorage;
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.apiClient = apiClient;
        this.authenticator = authenticator;
        this.credentials = credentials;
        this.tokenLifetime = tokenLifetime;
        this.tokenLifetimeUnit = tokenLifetimeUnit;
        this.callbackUri = callbackUri;

        this.strongAuthenticationStateSupplementalKey =
                strongAuthenticationState.getSupplementalKey();
        this.strongAuthenticationState = strongAuthenticationState.getState();

        this.nonce = RandomUtils.generateRandomHexEncoded(8);
        this.appToAppRedirectURL = appToAppRedirectURL;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token accessToken =
                persistentStorage
                        .get(
                                OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                OAuth2Token.class)
                        .orElseThrow(
                                () -> {
                                    logger.warn(
                                            "Failed to retrieve access token from persistent storage.");
                                    return SessionError.SESSION_EXPIRED.exception();
                                });

        if (accessToken.hasAccessExpired()) {
            if (!accessToken.canRefresh()) {
                logger.info("Access and refresh token expired.");
                throw SessionError.SESSION_EXPIRED.exception();
            }

            logger.info(
                    String.format(
                            "Trying to refresh access token. Issued: [%s] Access Expires: [%s] HasRefresh: [%b] Refresh Expires: [%s]",
                            new Date(accessToken.getIssuedAt() * 1000),
                            new Date(accessToken.getAccessExpireEpoch() * 1000),
                            !accessToken.isRefreshNullOrEmpty(),
                            accessToken.hasRefreshExpire()
                                    ? new Date(accessToken.getRefreshExpireEpoch() * 1000)
                                    : "N/A"));

            // Refresh token is not always present, if it's absent we fall back to the manual
            // authentication again.
            String refreshToken =
                    accessToken
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

            try {

                accessToken =
                        apiClient.refreshAccessToken(
                                refreshToken, authenticator.getClientCredentialScope());
            } catch (HttpResponseException e) {

                logger.info(
                        String.format("Refresh failed: %s", e.getResponse().getBody(String.class)));
                // This will "fix" the invalid_grant error temporarily while waiting for more log
                // data. It might also filter some other errors.
                throw SessionError.SESSION_EXPIRED.exception();
            }

            if (!accessToken.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            logger.info(
                    String.format(
                            "Refresh success. New token: Access Expires: [%s] HasRefresh: [%b] Refresh Expires: [%s]",
                            new Date(accessToken.getAccessExpireEpoch() * 1000),
                            !accessToken.isRefreshNullOrEmpty(),
                            accessToken.hasRefreshExpire()
                                    ? new Date(accessToken.getRefreshExpireEpoch() * 1000)
                                    : "N/A"));

            // Store the new accessToken on the persistent storage again.
            saveAccessToken(accessToken);

            // fall through.
        }

        // as AutoAuthenticate will only happen in case of Ais so need to instantiate Ais filter
        apiClient.instantiateAisAuthFilter(accessToken);
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        clientAccessToken =
                apiClient.requestClientCredentials(authenticator.getClientCredentialScope());
        if (!clientAccessToken.isValid()) {
            throw new IllegalStateException("Client access token is not valid.");
        }

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {

        URL authorizeUrl =
                apiClient.buildAuthorizeUrl(
                        strongAuthenticationState,
                        nonce,
                        authenticator.getClientCredentialScope(),
                        callbackUri,
                        appToAppRedirectURL);

        instantiateAuthFilter(clientAccessToken);

        // Let the agent add to or change the URL before we send it to the front-end.
        authorizeUrl =
                authenticator.decorateAuthorizeUrl(
                        authorizeUrl, strongAuthenticationState, nonce, callbackUri);

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

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {

        Map<String, String> callbackData =
                supplementalInformationHelper
                        .waitForSupplementalInformation(
                                strongAuthenticationStateSupplementalKey,
                                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);

        handleErrors(callbackData);

        String code =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.CODE)
                        .orElseThrow(
                                () ->
                                        new IllegalStateException(
                                                "callbackData did not contain code."));

        // todo: verify idToken{s_hash, c_hash}
        // TODO: Right now many banks don't give us idToken to verify, enable when this standard is
        // mandatory.
        //        String idToken = getCallbackElement(callbackData,
        // OpenIdConstants.CallbackParams.ID_TOKEN)
        //                .orElseThrow(() -> new IllegalStateException("callbackData did not contain
        // id_token."));

        OAuth2Token accessToken =
                apiClient.exchangeAccessCode(code, authenticator.getClientCredentialScope());

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

        saveAccessToken(accessToken);

        instantiateAuthFilter(accessToken);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private void saveAccessToken(OAuth2Token accessToken) {
        switch (authenticator.getClientCredentialScope()) {
            case PAYMENTS:
                persistentStorage.put(PersistentStorageKeys.PIS_ACCESS_TOKEN, accessToken);
                break;
            case ACCOUNTS:
            default:
                persistentStorage.put(PersistentStorageKeys.AIS_ACCESS_TOKEN, accessToken);
        }
    }

    private void instantiateAuthFilter(OAuth2Token oAuth2Token) {
        switch (authenticator.getClientCredentialScope()) {
            case PAYMENTS:
                apiClient.instantiatePisAuthFilter(oAuth2Token);
                break;
            case ACCOUNTS:
            default:
                apiClient.instantiateAisAuthFilter(oAuth2Token);
        }
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private Optional<String> getCallbackElement(Map<String, String> callbackData, String key) {
        String value = callbackData.getOrDefault(key, null);
        if (Strings.isNullOrEmpty(value)) {
            return Optional.empty();
        }

        return Optional.of(value);
    }

    private void handleErrors(Map<String, String> callbackData)
            throws AuthenticationException, AuthorizationException {
        Optional<String> error =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ERROR);
        Optional<String> errorDescription =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ERROR_DESCRIPTION);

        if (!error.isPresent()) {
            logger.info("OpenId callback success.");
            return;
        }

        String errorType = error.get();
        if (OpenIdConstants.Errors.ACCESS_DENIED.equalsIgnoreCase(errorType)) {
            logger.info(
                    String.format(
                            "OpenId ACCESS_DENIED callback: %s",
                            SerializationUtils.serializeToString(callbackData)));
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        throw new IllegalStateException(
                String.format("Unknown error: %s:%s.", errorType, errorDescription.orElse("")));
    }
}
