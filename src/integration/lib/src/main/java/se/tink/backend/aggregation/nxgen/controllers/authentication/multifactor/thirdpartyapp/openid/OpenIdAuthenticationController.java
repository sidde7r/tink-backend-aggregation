package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.List;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.ClientMode;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.OpenIdConstants.PersistentStorageKeys;
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
    private OAuth2Token clientOAuth2Token;
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

        // added to force users to relogin to fetch IdentityData for UKOB
        persistentStorage
                .get(PersistentStorageKeys.AIS_ACCOUNT_PERMISSIONS_GRANTED, List.class)
                .orElseThrow(
                        () -> {
                            logger.warn(
                                    "Failed to retrieve identity data permission from "
                                            + "persistent storage. So forcing user to authenticate again");
                            return SessionError.SESSION_EXPIRED.exception();
                        });

        OAuth2Token oAuth2Token =
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

        if (oAuth2Token.hasAccessExpired()) {
            if (!oAuth2Token.canRefresh()) {
                logger.info("Access and refresh token expired.");
                throw SessionError.SESSION_EXPIRED.exception();
            }

            logger.info(
                    String.format(
                            "Trying to refresh access token. Issued: [%s] Access Expires: [%s] HasRefresh: [%b] Refresh Expires: [%s]",
                            new Date(oAuth2Token.getIssuedAt() * 1000),
                            new Date(oAuth2Token.getAccessExpireEpoch() * 1000),
                            !oAuth2Token.isRefreshNullOrEmpty(),
                            oAuth2Token.hasRefreshExpire()
                                    ? new Date(oAuth2Token.getRefreshExpireEpoch() * 1000)
                                    : "N/A"));

            // Refresh token is not always present, if it's absent we fall back to the manual
            // authentication again.
            String refreshToken =
                    oAuth2Token
                            .getRefreshToken()
                            .orElseThrow(SessionError.SESSION_EXPIRED::exception);

            try {

                OAuth2Token refreshedOAuth2Token =
                        apiClient.refreshAccessToken(
                                refreshToken, authenticator.getClientCredentialScope());

                if (!refreshedOAuth2Token.isValid()) {
                    throw SessionError.SESSION_EXPIRED.exception();
                }

                if (refreshedOAuth2Token.hasRefreshExpire()) {
                    credentials.setSessionExpiryDate(
                            OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                                    refreshedOAuth2Token, tokenLifetime, tokenLifetimeUnit));
                }

                oAuth2Token = refreshedOAuth2Token.updateTokenWithOldToken(oAuth2Token);

            } catch (HttpResponseException e) {

                logger.info(
                        String.format("Refresh failed: %s", e.getResponse().getBody(String.class)));
                // This will "fix" the invalid_grant error temporarily while waiting for more log
                // data. It might also filter some other errors.
                throw SessionError.SESSION_EXPIRED.exception();
            }

            logger.info(
                    String.format(
                            "Refresh success. New token: Access Expires: [%s] HasRefresh: [%b] Refresh Expires: [%s]",
                            new Date(oAuth2Token.getAccessExpireEpoch() * 1000),
                            !oAuth2Token.isRefreshNullOrEmpty(),
                            oAuth2Token.hasRefreshExpire()
                                    ? new Date(oAuth2Token.getRefreshExpireEpoch() * 1000)
                                    : "N/A"));

            // Store the new accessToken on the persistent storage again.
            saveAccessToken(oAuth2Token);

            // fall through.
        }

        // as AutoAuthenticate will only happen in case of Ais so need to instantiate Ais filter
        apiClient.instantiateAisAuthFilter(oAuth2Token);
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        clientOAuth2Token =
                apiClient.requestClientCredentials(authenticator.getClientCredentialScope());
        if (!clientOAuth2Token.isValid()) {
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

        instantiateAuthFilter(clientOAuth2Token);

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

        OAuth2Token oAuth2Token =
                apiClient.exchangeAccessCode(code, authenticator.getClientCredentialScope());

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

        saveAccessToken(oAuth2Token);

        instantiateAuthFilter(oAuth2Token);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private void saveAccessToken(OAuth2Token oAuth2Token) {
        // only need to save the AIS toke in storage as we dont use pis token after payment is done.
        if (authenticator.getClientCredentialScope().equals(ClientMode.ACCOUNTS)) {
            persistentStorage.rotateStorageValue(
                    PersistentStorageKeys.AIS_ACCESS_TOKEN, oAuth2Token);
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
