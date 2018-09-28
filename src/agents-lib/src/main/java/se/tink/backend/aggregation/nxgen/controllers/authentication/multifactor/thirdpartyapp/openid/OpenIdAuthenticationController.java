package se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid;

import com.google.common.base.Strings;
import java.security.SecureRandom;
import java.util.Base64;
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
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppAuthenticator;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponse;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppResponseImpl;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.ThirdPartyAppStatus;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.ProviderConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.configuration.SoftwareStatement;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.openid.rpc.AccountPermissionResponse;
import se.tink.backend.aggregation.nxgen.controllers.utils.SupplementalInformationController;
import se.tink.backend.aggregation.nxgen.http.TinkHttpClient;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.common.payloads.ThirdPartyAppAuthenticationPayload;
import se.tink.libraries.i18n.LocalizableKey;

public class OpenIdAuthenticationController implements AutoAuthenticator, ThirdPartyAppAuthenticator<String> {

    // This wait time is for the whole user authentication. Different banks have different cumbersome
    // authentication flows.
    private static final long WAIT_FOR_MINUTES = 9;

    private static final Random random = new SecureRandom();
    private static final Base64.Encoder encoder = Base64.getUrlEncoder();

    private final PersistentStorage persistentStorage;
    private final SupplementalInformationController supplementalInformationController;
    private final OpenIdAuthenticator authenticator;

    private final SoftwareStatement softwareStatement;
    private final ProviderConfiguration providerConfiguration;

    private final OpenIdApiClient apiClient;
    private final String state;
    private AuthenticationToken clientAuthToken;

    public OpenIdAuthenticationController(PersistentStorage persistentStorage,
            SupplementalInformationController supplementalInformationController,
            TinkHttpClient httpClient,
            OpenIdAuthenticator authenticator,
            SoftwareStatement softwareStatement,
            ProviderConfiguration providerConfiguration) {
        this.persistentStorage = persistentStorage;
        this.supplementalInformationController = supplementalInformationController;
        this.authenticator = authenticator;
        this.softwareStatement = softwareStatement;
        this.providerConfiguration = providerConfiguration;

        this.apiClient = new OpenIdApiClient(httpClient, softwareStatement, providerConfiguration);
        this.state = generateRandomState();
    }

    private static String generateRandomState() {
        byte[] randomData = new byte[32];
        random.nextBytes(randomData);
        return encoder.encodeToString(randomData);
    }

    @Override
    public void autoAuthenticate() throws SessionException, BankServiceException {
        AuthenticationToken authToken = persistentStorage.get(OpenIdConstants.PersistentStorageKeys.AUTH_TOKEN,
                AuthenticationToken.class)
                .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (authToken.hasExpired()) {
            // Refresh token is not always present, if it's absent we fall back to the manual authentication again.
            String refreshToken = authToken.getRefreshToken().orElseThrow(SessionError.SESSION_EXPIRED::exception);

            authToken = apiClient.refreshAuthenticationToken(refreshToken);
            if (!authToken.isValid()) {
                throw SessionError.SESSION_EXPIRED.exception();
            }

            // Store the new authToken on the persistent storage again.
            persistentStorage.put(OpenIdConstants.PersistentStorageKeys.AUTH_TOKEN, authToken);

            // fall through.
        }

        registerAuthToken(authToken);
    }

    @Override
    public ThirdPartyAppResponse<String> init() {
        // make openid requests to init the auth.

        clientAuthToken = apiClient.requestClientCredentials();
        if (!clientAuthToken.isValid()) {
            throw new IllegalStateException("Client auth token is not valid.");
        }

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.WAITING);
    }

    @Override
    public ThirdPartyAppAuthenticationPayload getAppPayload() {
        AccountPermissionResponse accountPermissionResponse = apiClient.requestAccountsApi(clientAuthToken);

        URL authorizeUrl = apiClient.buildAuthorizeUrl(state,
                accountPermissionResponse.getData().getAccountRequestId());

        // Let the agent add to or change the URL before we send it to the front-end.
        authorizeUrl = authenticator.buildAuthorizeUrl(authorizeUrl);


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

        String code = getCallbackItem(callbackData, OpenIdConstants.CallbackParams.CODE);

        // todo: verify idToken{s_hash, c_hash}
        //String idToken = getCallbackItem(callbackData, OpenIdConstants.CallbackParams.ID_TOKEN);

        AuthenticationToken authToken = apiClient.exchangeAccessCode(code);

        if (!authToken.isValid()) {
            throw new IllegalStateException("Invalid auth token.");
        }

        if (!authToken.isBearer()) {
            throw new IllegalStateException(String.format("Unknown token type '%s'.", authToken.getTokenType()));
        }

        persistentStorage.put(OpenIdConstants.PersistentStorageKeys.AUTH_TOKEN, authToken);

        registerAuthToken(authToken);

        return ThirdPartyAppResponseImpl.create(ThirdPartyAppStatus.DONE);
    }

    @Override
    public Optional<LocalizableKey> getUserErrorMessageFor(ThirdPartyAppStatus status) {
        return Optional.empty();
    }

    private String getCallbackItem(Map<String, String> callbackData, String key) {
        String value = callbackData.getOrDefault(key, null);
        if (Strings.isNullOrEmpty(value)) {
            throw new IllegalStateException(String.format("callbackData did not contain '%s'.", key));
        }

        return value;
    }

    private String formatSupplementalKey(String key) {
        // Ensure third party callback information does not collide with other Supplemental Information by using a
        // prefix. This prefix is the same in MAIN.
        return String.format("tpcb_%s", key);
    }

    private void registerAuthToken(AuthenticationToken authToken) {
        apiClient.registerAuthFilter(authToken);
    }
}
