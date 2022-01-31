package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid;

import com.google.common.base.Strings;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.time.temporal.TemporalUnit;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.LoginException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.entity.ErrorEntity;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data.AuthenticationDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data.ConsentDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.ErrorDescriptions;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.entities.ClientMode;
import se.tink.backend.aggregation.logmasker.LogMasker;
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
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class OpenIdAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private static final int DEFAULT_TOKEN_LIFETIME = 90;
    private static final TemporalUnit DEFAULT_TOKEN_LIFETIME_UNIT = ChronoUnit.DAYS;

    private final AuthenticationDataStorage authenticationDataStorage;
    private final ConsentDataStorage consentDataStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OpenIdApiClient apiClient;
    private final OpenIdAuthenticator authenticator;
    private final Credentials credentials;
    private final int tokenLifetime;
    private final TemporalUnit tokenLifetimeUnit;

    private final String strongAuthenticationState;
    private final String strongAuthenticationStateSupplementalKey;

    private final RandomValueGenerator randomValueGenerator;
    private final String callbackUri;
    private final OpenIdAuthenticationValidator authenticationValidator;
    private final LogMasker logMasker;

    private final RetryExecutor retryExecutor = new RetryExecutor();

    public OpenIdAuthenticationController(
            PersistentStorage persistentStorage,
            SupplementalInformationHelper supplementalInformationHelper,
            OpenIdApiClient apiClient,
            OpenIdAuthenticator authenticator,
            Credentials credentials,
            StrongAuthenticationState strongAuthenticationState,
            String callbackUri,
            RandomValueGenerator randomValueGenerator,
            OpenIdAuthenticationValidator authenticationValidator,
            LogMasker logMasker) {
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
                authenticationValidator,
                logMasker);
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
            OpenIdAuthenticationValidator authenticationValidator,
            LogMasker logMasker) {
        this.authenticationDataStorage = new AuthenticationDataStorage(persistentStorage);
        this.consentDataStorage = new ConsentDataStorage(persistentStorage);
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
        this.logMasker = logMasker;

        this.retryExecutor.setRetryPolicy(new RetryPolicy(2, RuntimeException.class));
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        OAuth2Token clientOAuth2Token = apiClient.requestClientCredentials(ClientMode.ACCOUNTS);
        authenticationValidator.validateClientToken(clientOAuth2Token);
        instantiateAuthFilter(clientOAuth2Token);
        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        String nonce = randomValueGenerator.generateRandomHexEncoded(8);
        return ThirdPartyAppAuthenticationPayload.of(
                authenticator.createAuthorizeUrl(
                        strongAuthenticationState, nonce, callbackUri, ClientMode.ACCOUNTS));
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
                                            "[OpenIdAuthenticationController] callbackData did not "
                                                    + "contain code. CallbackUri: {}, "
                                                    + "Data received: {}",
                                            callbackUri,
                                            SerializationUtils.serializeToString(callbackData));
                                    throw SessionError.SESSION_EXPIRED.exception();
                                });

        String state = getCallbackElement(callbackData, OpenIdConstants.Params.STATE).orElse(null);

        Optional<String> idToken =
                getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ID_TOKEN);

        if (idToken.isPresent()) {
            authenticationValidator.validateIdToken(idToken.get(), code, state);
        } else {
            log.warn(
                    "[OpenIdAuthenticationController] ID Token (code and state) "
                            + "validation - no token provided");
        }

        OAuth2Token oAuth2Token = retryExecutor.execute(() -> apiClient.exchangeAccessCode(code));

        log.info(
                "[OpenIdAuthenticationController] OAuth2 token received from bank: {}",
                oAuth2Token.toMaskedString(logMasker));

        authenticationValidator.validateRefreshableAccessToken(oAuth2Token);

        credentials.setSessionExpiryDate(
                OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                        oAuth2Token, tokenLifetime, tokenLifetimeUnit));

        authenticationDataStorage.saveStrongAuthenticationTime();
        authenticationDataStorage.saveAccessToken(oAuth2Token);

        instantiateAuthFilter(oAuth2Token);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {

        OAuth2Token oAuth2Token = authenticationDataStorage.restoreAccessToken();

        log.info(
                "[OpenIdAuthenticationController] OAuth2 token retrieved from persistent storage {} ",
                oAuth2Token.toMaskedString(logMasker));

        if (oAuth2Token.canUseAccessToken()) {
            apiClient.instantiateAisAuthFilter(oAuth2Token);
            return;
        }

        if (oAuth2Token.canNotRefreshAccessToken()) {
            log.info(
                    "[OpenIdAuthenticationController] Access token has expired and refreshing "
                            + "impossible. Expiring the session.");
            consentDataStorage.removeConsentId();
            authenticationDataStorage.removeAccessToken();
            throw SessionError.SESSION_EXPIRED.exception();
        }

        OAuth2Token refreshedToken = refreshAccessToken(oAuth2Token);
        authenticationDataStorage.saveAccessToken(refreshedToken);
        oAuth2Token = refreshedToken;

        apiClient.instantiateAisAuthFilter(oAuth2Token);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private OAuth2Token refreshAccessToken(OAuth2Token oAuth2Token) throws SessionException {
        log.info(
                "[OpenIdAuthenticationController] Trying to refresh access token. "
                        + "Issued: [{}] Access Expires: [{}] "
                        + "Has Refresh Token: [{}] Refresh Token Expires: [{}]",
                LocalDateTime.ofEpochSecond(oAuth2Token.getIssuedAt(), 0, ZoneOffset.UTC),
                LocalDateTime.ofEpochSecond(oAuth2Token.getAccessExpireEpoch(), 0, ZoneOffset.UTC),
                oAuth2Token.isRefreshNullOrEmpty(),
                oAuth2Token.isRefreshTokenExpirationPeriodSpecified()
                        ? LocalDateTime.ofEpochSecond(
                                oAuth2Token.getRefreshExpireEpoch(), 0, ZoneOffset.UTC)
                        : "N/A");

        String refreshToken = oAuth2Token.getOptionalRefreshToken().get();
        try {
            OAuth2Token refreshedOAuth2Token =
                    retryExecutor.execute(
                            () -> apiClient.refreshAccessToken(refreshToken, ClientMode.ACCOUNTS));

            if (!refreshedOAuth2Token.isValid()) {
                log.warn(
                        "[OpenIdAuthenticationController] Access token refreshed, but it is invalid. "
                                + "Expiring the session.");
                throw SessionError.SESSION_EXPIRED.exception();
            }

            if (refreshedOAuth2Token.isRefreshTokenExpirationPeriodSpecified()) {
                credentials.setSessionExpiryDate(
                        OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                                refreshedOAuth2Token, tokenLifetime, tokenLifetimeUnit));
            }

            oAuth2Token = refreshedOAuth2Token.updateTokenWithOldToken(oAuth2Token);

        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() >= 500) {
                log.warn(
                        "[OpenIdAuthenticationController] Bank side error (status code {}) during "
                                + "refreshing token",
                        e.getResponse().getStatus());
                throw BankServiceError.BANK_SIDE_FAILURE.exception(e);
            }
            log.error(
                    "[OpenIdAuthenticationController] Access token refresh failed: {}",
                    e.getResponse().getBody(String.class));

            throw SessionError.SESSION_EXPIRED.exception();
        }
        log.info(
                "[OpenIdAuthenticationController] Token refreshed successfully. New token - Access "
                        + "Expires: [{}] Has Refresh Token: [{}] Refresh Expires: [{}]",
                LocalDateTime.ofEpochSecond(oAuth2Token.getAccessExpireEpoch(), 0, ZoneOffset.UTC),
                oAuth2Token.isRefreshNullOrEmpty(),
                oAuth2Token.isRefreshTokenExpirationPeriodSpecified()
                        ? LocalDateTime.ofEpochSecond(
                                oAuth2Token.getRefreshExpireEpoch(), 0, ZoneOffset.UTC)
                        : "N/A");
        return oAuth2Token;
    }

    private void instantiateAuthFilter(OAuth2Token oAuth2Token) {
        apiClient.instantiateAisAuthFilter(oAuth2Token);
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
            log.info("[OpenIdAuthenticationController] OpenId callback success.");
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

        String consentId = consentDataStorage.restoreConsentId();

        log.info(
                "[OpenIdAuthenticationController] OpenId callback data: {} for consentId {}",
                serializedCallbackData,
                consentId);

        if (OpenIdConstants.Errors.ACCESS_DENIED.equalsIgnoreCase(errorType)
                || OpenIdConstants.Errors.LOGIN_REQUIRED.equalsIgnoreCase(errorType)) {
            // Store error information to make it possible for agent to determine cause and
            // give end user a proper error message.
            apiClient.storeOpenIdError(ErrorEntity.create(errorType, errorDescription));
            throw LoginError.INCORRECT_CREDENTIALS.exception();

        } else if (OpenIdConstants.Errors.SERVER_ERROR.equalsIgnoreCase(errorType)) {
            if (ErrorDescriptions.SERVER_ERROR_PROCESSING.equalsIgnoreCase(errorDescription)) {
                throw ThirdPartyAppError.CANCELLED.exception(errorDescription);
            }
            throw BankServiceError.BANK_SIDE_FAILURE.exception(errorDescription);

        } else if (OpenIdConstants.Errors.TEMPORARILY_UNAVAILABLE.equalsIgnoreCase(errorType)) {
            throw BankServiceError.NO_BANK_SERVICE.exception(errorDescription);

        } else if (OpenIdConstants.Errors.UNAUTHORISED.equalsIgnoreCase(errorType)) {
            throw BankServiceError.SESSION_TERMINATED.exception(errorDescription);

        } else if (OpenIdConstants.Errors.INVALID_INTENT_ID.equalsIgnoreCase(errorType)) {
            throw SessionError.CONSENT_INVALID.exception();
        }

        throw new IllegalStateException(
                String.format(
                        "[OpenIdAuthenticationController] Unknown error with details: "
                                + "{errorType: %s, errorDescription: %s}",
                        errorType, errorDescription));
    }
}
