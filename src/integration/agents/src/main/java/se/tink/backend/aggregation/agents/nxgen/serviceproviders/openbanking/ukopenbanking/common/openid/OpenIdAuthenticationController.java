package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.BANK_SIDE_FAILURE;
import static se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError.NO_BANK_SERVICE;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.PersistentStorageKeys;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.nxgen.agents.componentproviders.generated.randomness.RandomValueGenerator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.authenticator.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.constants.ThirdPartyAppConstants;
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

@Slf4j
public class OpenIdAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    protected final PersistentStorage persistentStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OpenIdApiClient apiClient;
    protected final OpenIdAuthenticator authenticator;
    private final Credentials credentials;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;

    private final String strongAuthenticationState;
    private final String strongAuthenticationStateSupplementalKey;

    private final RandomValueGenerator randomValueGenerator;
    private final String callbackUri;
    private final OpenIdAuthenticationValidator authenticationValidator;

    public OpenIdAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OpenIdApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            RandomValueGenerator randomValueGenerator,
            OpenIdAuthenticationValidator authenticationValidator) {
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
                randomValueGenerator,
                authenticationValidator);
    }

    private OpenIdAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OpenIdApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            int tokenLifetime,
            TemporalUnit tokenLifetimeUnit,
            RandomValueGenerator randomValueGenerator,
            OpenIdAuthenticationValidator authenticationValidator) {
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
        this.randomValueGenerator = randomValueGenerator;
        this.authenticationValidator = authenticationValidator;
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token oAuth2Token =
                persistentStorage
                        .get(
                                OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN,
                                OAuth2Token.class)
                        .orElseThrow(
                                () -> {
                                    log.warn(
                                            "Failed to retrieve access token from persistent storage.");
                                    return SessionError.SESSION_EXPIRED.exception();
                                });

        if (oAuth2Token.hasAccessExpired()) {
            if (!oAuth2Token.canRefresh()) {
                log.info(
                        "Access token has expired and refreshing impossible. Expiring the session.");
                cleanAuthenticationPersistentStorage();
                throw SessionError.SESSION_EXPIRED.exception();
            } else {
                OAuth2Token refreshedToken = refreshAccessToken(oAuth2Token);
                saveAccessToken(refreshedToken);
                oAuth2Token = refreshedToken;

                String consentId =
                        persistentStorage
                                .get(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID, String.class)
                                .orElse(StringUtils.EMPTY);

                if (consentId.equals(OpenIdAuthenticatorConstants.CONSENT_ERROR_OCCURRED)) {
                    cleanAuthenticationPersistentStorage();
                    throw SessionError.CONSENT_INVALID.exception(
                            "These credentials were marked with CONSENT_ERROR_OCCURRED flag in the past. Expiring the session.");
                }
            }
        }

        // as AutoAuthenticate will only happen in case of Ais so need to instantiate Ais filter
        apiClient.instantiateAisAuthFilter(oAuth2Token);
    }

    private void cleanAuthenticationPersistentStorage() {
        persistentStorage.remove(PersistentStorageKeys.AIS_ACCOUNT_CONSENT_ID);
        persistentStorage.remove(PersistentStorageKeys.AIS_ACCESS_TOKEN);
    }

    private OAuth2Token refreshAccessToken(OAuth2Token oAuth2Token) throws SessionException {
        log.info(
                "Trying to refresh access token. Issued: [{}] Access Expires: [{}] HasRefresh: [{}] Refresh Expires: [{}]",
                new Date(oAuth2Token.getIssuedAt() * 1000),
                new Date(oAuth2Token.getAccessExpireEpoch() * 1000),
                !oAuth2Token.isRefreshNullOrEmpty(),
                oAuth2Token.hasRefreshExpire()
                        ? new Date(oAuth2Token.getRefreshExpireEpoch() * 1000)
                        : "N/A");

        String refreshToken = oAuth2Token.getRefreshToken().get();
        try {
            OAuth2Token refreshedOAuth2Token =
                    apiClient.refreshAccessToken(refreshToken, ClientMode.ACCOUNTS);

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

            log.error("Refresh failed: {}", e.getResponse().getBody(String.class));
            // This will "fix" the invalid_grant error temporarily while waiting for more log
            // data. It might also filter some other errors.
            throw SessionError.SESSION_EXPIRED.exception();
        }

        log.info(
                "Refresh success. New token: Access Expires: [{}] HasRefresh: [{}] Refresh Expires: [{}]",
                new Date(oAuth2Token.getAccessExpireEpoch() * 1000),
                !oAuth2Token.isRefreshNullOrEmpty(),
                oAuth2Token.hasRefreshExpire()
                        ? new Date(oAuth2Token.getRefreshExpireEpoch() * 1000)
                        : "N/A");
        return oAuth2Token;
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        OAuth2Token clientOAuth2Token = apiClient.requestClientCredentials(ClientMode.ACCOUNTS);

        authenticationValidator.validateClientToken(clientOAuth2Token);

        instantiateAuthFilter(clientOAuth2Token);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    // Prepare third party app payload containing authentication url
    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        String nonce = randomValueGenerator.generateRandomHexEncoded(8);

        URL authorizeUrl =
                apiClient.buildAuthorizeUrl(
                        strongAuthenticationState, nonce, ClientMode.ACCOUNTS, callbackUri);

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
                        .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);

        handlePossibleErrors(callbackData);

        String code =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.CODE)
                        .orElseGet(
                                () -> {
                                    log.error(
                                            "callbackData did not contain code. CallbackUri: {}, Data received: {}",
                                            callbackUri,
                                            SerializationUtils.serializeToString(callbackData));
                                    throw new IllegalStateException(
                                            "callbackData did not contain code.");
                                });

        String state = getCallbackElement(callbackData, OpenIdConstants.Params.STATE).orElse(null);

        Optional<String> idToken =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ID_TOKEN);

        if (idToken.isPresent()) {
            authenticationValidator.validateIdToken(idToken.get(), code, state);
        } else {
            log.warn("ID Token (code and state) validation - no token provided");
        }

        OAuth2Token oAuth2Token = apiClient.exchangeAccessCode(code);

        authenticationValidator.validateAccessToken(oAuth2Token);

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        oAuth2Token, tokenLifetime, tokenLifetimeUnit));

        saveStrongAuthenticationTime();
        saveAccessToken(oAuth2Token);

        instantiateAuthFilter(oAuth2Token);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    private void saveStrongAuthenticationTime() {
        persistentStorage.put(
                OpenIdConstants.PersistentStorageKeys.LAST_SCA_TIME,
                LocalDateTime.now().toString());
    }

    private void saveAccessToken(OAuth2Token oAuth2Token) {
        persistentStorage.rotateStorageValue(
                OpenIdConstants.PersistentStorageKeys.AIS_ACCESS_TOKEN, oAuth2Token);
    }

    private void instantiateAuthFilter(OAuth2Token oAuth2Token) {
        apiClient.instantiateAisAuthFilter(oAuth2Token);
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

    private void handlePossibleErrors(Map<String, String> callbackData)
            throws AuthenticationException {
        Optional<String> error =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ERROR);
        Optional<String> errorDescription =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ERROR_DESCRIPTION);

        if (!error.isPresent()) {
            log.info("OpenId callback success.");
            return;
        }

        handleError(
                SerializationUtils.serializeToString(callbackData),
                error.orElse(""),
                errorDescription.orElse(""));
    }

    private void handleError(
            String serializedCallbackData, String errorType, String errorDescription)
            throws LoginException {

        if (OpenIdConstants.Errors.ACCESS_DENIED.equalsIgnoreCase(errorType)
                || OpenIdConstants.Errors.LOGIN_REQUIRED.equalsIgnoreCase(errorType)) {

            log.info("OpenId {} callback: {}", errorType, serializedCallbackData);

            // Store error information to make it possible for agent to determine cause and
            // give end user a proper error message.
            apiClient.storeOpenIdError(ErrorEntity.create(errorType, errorDescription));

            throw LoginError.INCORRECT_CREDENTIALS.exception();
        } else if (OpenIdConstants.Errors.SERVER_ERROR.equalsIgnoreCase(errorType)) {
            throw BANK_SIDE_FAILURE.exception(errorDescription);
        } else if (OpenIdConstants.Errors.TEMPORARILY_UNAVAILABLE.equalsIgnoreCase(errorType)) {
            throw NO_BANK_SERVICE.exception(errorDescription);
        }

        throw new IllegalStateException(
                String.format("Unknown error: %s:%s.", errorType, errorDescription));
    }
}
