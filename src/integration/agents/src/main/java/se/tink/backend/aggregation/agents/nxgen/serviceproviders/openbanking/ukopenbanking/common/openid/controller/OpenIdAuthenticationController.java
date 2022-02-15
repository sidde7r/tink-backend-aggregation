package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.controller;

import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Token.DEFAULT_TOKEN_LIFETIME;
import static se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants.Token.DEFAULT_TOKEN_LIFETIME_UNIT;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.exceptions.errors.ThirdPartyAppError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data.AuthenticationDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.ais.base.storage.data.ConsentDataStorage;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdApiClient;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticationValidator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdAuthenticator;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.ukopenbanking.common.openid.OpenIdConstants;
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
import se.tink.backend.aggregation.nxgen.http.exceptions.client.HttpClientException;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.libraries.i18n_aggregation.LocalizableKey;
import se.tink.libraries.retrypolicy.RetryExecutor;
import se.tink.libraries.retrypolicy.RetryPolicy;
import se.tink.libraries.serialization.utils.SerializationUtils;

@Slf4j
public class OpenIdAuthenticationController
        implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    private final AuthenticationDataStorage authenticationDataStorage;
    private final ConsentDataStorage consentDataStorage;
    private final SupplementalInformationHelper supplementalInformationHelper;
    private final OpenIdApiClient apiClient;
    private final OpenIdAuthenticator authenticator;
    private final Credentials credentials;
    private final StrongAuthenticationState strongAuthenticationState;
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
        this.authenticationDataStorage = new AuthenticationDataStorage(persistentStorage);
        this.consentDataStorage = new ConsentDataStorage(persistentStorage);
        this.supplementalInformationHelper = supplementalInformationHelper;
        this.apiClient = apiClient;
        this.authenticator = authenticator;
        this.credentials = credentials;
        this.callbackUri = callbackUri;
        this.strongAuthenticationState = strongAuthenticationState;
        this.randomValueGenerator = randomValueGenerator;
        this.authenticationValidator = authenticationValidator;
        this.logMasker = logMasker;
        this.retryExecutor.setRetryPolicy(new RetryPolicy(2, RuntimeException.class));
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        OAuth2Token clientOAuth2Token = requestClientToken();
        apiClient.instantiateAisAuthFilter(clientOAuth2Token);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        String state = strongAuthenticationState.getState();
        String nonce = randomValueGenerator.generateRandomHexEncoded(8);

        return ThirdPartyAppAuthenticationPayload.of(
                authenticator.createAuthorizeUrl(state, nonce, callbackUri, ClientMode.ACCOUNTS));
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference)
            throws AuthenticationException, AuthorizationException {
        String scaSupplementalKey = strongAuthenticationState.getSupplementalKey();

        Map<String, String> callbackData =
                supplementalInformationHelper
                        .waitForSupplementalInformation(
                                scaSupplementalKey,
                                ThirdPartyAppConstants.WAIT_FOR_MINUTES,
                                TimeUnit.MINUTES)
                        .orElseThrow(ThirdPartyAppError.TIMED_OUT::exception);

        ErrorHandler errorHandler = new OpenIdAuthenticationErrorHandler(consentDataStorage);
        errorHandler.handle(callbackData);

        String code =
                CallbackDataExtractor.get(callbackData, OpenIdConstants.CallbackParams.CODE)
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

        String state =
                CallbackDataExtractor.get(callbackData, OpenIdConstants.Params.STATE).orElse(null);

        Optional<String> idToken =
                CallbackDataExtractor.get(callbackData, OpenIdConstants.CallbackParams.ID_TOKEN);

        if (idToken.isPresent()) {
            authenticationValidator.validateIdToken(idToken.get(), code, state);
        } else {
            log.warn(
                    "[OpenIdAuthenticationController] ID Token (code and state) "
                            + "validation - no token provided");
        }

        try {
            OAuth2Token oAuth2Token =
                    retryExecutor.execute(() -> apiClient.exchangeAccessCode(code));

            log.info(
                    "[OpenIdAuthenticationController] OAuth2 token received from bank: {}",
                    oAuth2Token.toMaskedString(logMasker));

            authenticationValidator.validateRefreshableAccessToken(oAuth2Token);

            credentials.setSessionExpiryDate(
                    OpenBankingTokenExpirationDateHelper.getExpirationDateFrom(
                            oAuth2Token, DEFAULT_TOKEN_LIFETIME, DEFAULT_TOKEN_LIFETIME_UNIT));

            authenticationDataStorage.saveStrongAuthenticationTime();
            authenticationDataStorage.saveAccessToken(oAuth2Token);
            apiClient.instantiateAisAuthFilter(oAuth2Token);

        } catch (HttpResponseException e) {
            log.error(
                    "[{}] Exchange of access code failed: {}",
                    OpenIdAuthenticationController.class.getSimpleName(),
                    e.getResponse().getBody(String.class));
            throw SessionError.SESSION_EXPIRED.exception();

        } catch (HttpClientException e) {
            log.error(
                    "[{}] Failure of processing the HTTP request or response: {}",
                    OpenIdAuthenticationController.class.getSimpleName(),
                    e.getMessage());
            throw SessionError.SESSION_EXPIRED.exception();
        }

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

    private OAuth2Token requestClientToken() {
        ClientTokenRequester clientTokenRequester =
                new OpenIdClientTokenRequester(apiClient, authenticationValidator);
        return clientTokenRequester.requestClientToken();
    }

    private OAuth2Token refreshAccessToken(OAuth2Token oAuth2Token) throws SessionException {
        AccessTokenRefresher accessTokenRefresher =
                new OpenIdAccessTokenRefresher(apiClient, credentials);
        return accessTokenRefresher.refresh(oAuth2Token);
    }
}
