package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.utils.random.RandomUtils;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.common.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;
import se.tink.libraries.serialization.utils.SerializationUtils;

public class OpenIdAuthenticationController implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {
    private static final AggregationLogger log = new AggregationLogger(OpenIdAuthenticationController.class);

    // This wait time is for the whole user authentication. Different banks have different cumbersome
    // authentication flows.
    private static final long WAIT_FOR_MINUTES = 9;

    private static final Random random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    private final PersistentStorage persistentStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final OpenIdApiClient apiClient;
    private final OpenIdAuthenticator authenticator;

    private final String state;
    private final String nonce;
    private OAuth2Token clientAccessToken;

    public OpenIdAuthenticationController(PersistentStorage persistentStorage,
            SupplementalInformationController supplementalInformationController,
            OpenIdApiClient apiClient,
            OpenIdAuthenticator authenticator) {
        this.persistentStorage = persistentStorage;
        this.supplementalInformationController = supplementalInformationController;
        this.apiClient = apiClient;
        this.authenticator = authenticator;

        this.state = RandomUtils.generateRandomBase64UrlEncoded(32);
        this.nonce = RandomUtils.generateRandomBase64UrlEncoded(32);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        OAuth2Token accessToken = persistentStorage.get(OpenIdConstants.PersistentStorageKeys.ACCESS_TOKEN,
                OAuth2Token.class)
                .orElseThrow(() -> {
                    log.warn("Failed to retrieve access token.");
                    return SessionError.SESSION_EXPIRED.exception();
                });

        if (accessToken.hasAccessExpired()) {
            if (!accessToken.canRefresh()) {
                log.info("Access and refresh token expired.");
                throw SessionError.SESSION_EXPIRED.exception();
            }

            log.info(String.format(
                    "Trying to refresh access token. Issued: [%s] Access Expires: [%s] HasRefresh: [%b] Refresh Expires: [%s]",
                    new Date(accessToken.getIssuedAt() * 1000),
                    new Date(accessToken.getAccessExpireEpoch() * 1000),
                    !accessToken.isRefreshNullOrEmpty(),
                    accessToken.hasRefreshExpire() ? new Date(accessToken.getRefreshExpireEpoch() * 1000) : "N/A"));

            // Refresh token is not always present, if it's absent we fall back to the manual authentication again.
            String refreshToken = accessToken.getRefreshToken().orElseThrow(SessionError.SESSION_EXPIRED::exception);

            try {

                accessToken = apiClient.refreshAccessToken(refreshToken);
            } catch (HttpResponseException e) {

                log.info(String.format("Refresh failed: %s", e.getResponse().getBody(String.class)));
                // This will "fix" the invalid_grant error temporarily while waiting for more log data. It might also filter some other errors.
                throw SessionError.SESSION_EXPIRED.exception();
            }

            if (!accessToken.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            log.info(String.format(
                    "Refresh success. New token: Access Expires: [%s] HasRefresh: [%b] Refresh Expires: [%s]",
                    new Date(accessToken.getAccessExpireEpoch() * 1000),
                    !accessToken.isRefreshNullOrEmpty(),
                    accessToken.hasRefreshExpire() ? new Date(accessToken.getRefreshExpireEpoch() * 1000) : "N/A"));

            // Store the new accessToken on the persistent storage again.
            persistentStorage.put(OpenIdConstants.PersistentStorageKeys.ACCESS_TOKEN, accessToken);

            // fall through.
        }

        apiClient.attachAuthFilter(accessToken);
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        clientAccessToken = apiClient.requestClientCredentials();
        if (!clientAccessToken.isValid()) {
            throw new IllegalStateException("Client access token is not valid.");
        }

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {

        URL authorizeUrl = apiClient.buildAuthorizeUrl(state, nonce);

        apiClient.attachAuthFilter(clientAccessToken);
        try {
            // Let the agent add to or change the URL before we send it to the front-end.
            authorizeUrl = authenticator.decorateAuthorizeUrl(authorizeUrl, state, nonce);
        } finally {
            apiClient.detachAuthFilter();
        }

        ThirdPartyAppAuthenticationPayload payload = new ThirdPartyAppAuthenticationPayload();

        ThirdPartyAppAuthenticationPayload.Android androidPayload = new ThirdPartyAppAuthenticationPayload.Android();
        androidPayload.setIntent(authorizeUrl.get());
        payload.setAndroid(androidPayload);

        ThirdPartyAppAuthenticationPayload.Ios iOsPayload = new ThirdPartyAppAuthenticationPayload.Ios();
        iOsPayload.setAppScheme(authorizeUrl.getScheme());
        iOsPayload.setDeepLinkUrl(authorizeUrl.get());
        payload.setIos(iOsPayload);

        return payload;
    }

    @Override
    public ThirdPartyAppResponse<String> collect(String reference) throws AuthenticationException,
            AuthorizationException {

        Map<String, String> callbackData = supplementalInformationController.waitForSupplementalInformation(
                formatSupplementalKey(state),
                WAIT_FOR_MINUTES,
                TimeUnit.MINUTES
        ).orElseThrow(LoginError.INCORRECT_CREDENTIALS::exception);

        handleErrors(callbackData);

        String code = getCallbackElement(callbackData, OpenIdConstants.CallbackParams.CODE)
                .orElseThrow(() -> new IllegalStateException("callbackData did not contain code."));

        // todo: verify idToken{s_hash, c_hash}
        // TODO: Right now many banks don't give us idToken to verify, enable when this standard is mandatory.
        //        String idToken = getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ID_TOKEN)
        //                .orElseThrow(() -> new IllegalStateException("callbackData did not contain id_token."));

        OAuth2Token accessToken = apiClient.exchangeAccessCode(code);

        if (!accessToken.isValid()) {
            throw new IllegalStateException("Invalid access token.");
        }

        if (!accessToken.isBearer()) {
            throw new IllegalStateException(String.format("Unknown token type '%s'.", accessToken.getTokenType()));
        }

        persistentStorage.put(OpenIdConstants.PersistentStorageKeys.ACCESS_TOKEN, accessToken);

        apiClient.attachAuthFilter(accessToken);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
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

    private void handleErrors(Map<String, String> callbackData) throws AuthenticationException,
            AuthorizationException {
        Optional<String> error = getCallbackElement(callbackData, OpenIdConstants.CallbackParams.ERROR);
        Optional<String> errorDescription = getCallbackElement(callbackData,
                OpenIdConstants.CallbackParams.ERROR_DESCRIPTION);

        if (!error.isPresent()) {
            log.info("OpenId callback success.");
            return;
        }

        String errorType = error.get();
        if (OpenIdConstants.Errors.ACCESS_DENIED.equalsIgnoreCase(errorType)) {
            log.info(String.format("OpenId ACCESS_DENIED callback: %s",
                    SerializationUtils.serializeToString(callbackData)));
            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }

        throw new IllegalStateException(
                String.format(
                        "Unknown error: %s:%s.",
                        errorType,
                        errorDescription.orElse("")
                )
        );
    }

    private String formatSupplementalKey(String key) {
        // Ensure third party callback information does not collide with other Supplemental Information by using a
        // prefix. This prefix is the same in MAIN.
        return String.format("tpcb_%s", key);
    }
}
