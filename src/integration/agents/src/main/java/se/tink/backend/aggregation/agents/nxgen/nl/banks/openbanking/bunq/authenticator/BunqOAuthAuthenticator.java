package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator;

import java.security.KeyPair;
import se.tink.backend.aggregation.agents.exceptions.BankServiceException;
import se.tink.backend.aggregation.agents.exceptions.errors.BankServiceError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.AddOAuthClientIdResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionUserAsPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.GetClientIdAndSecretResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.RegisterAsPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.TokenExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration.BunqConfiguration;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.authenticator.rpc.TokenEntity;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

// The OAuth flow for Bunq is different than the rest, they use the access token as an "API key" for
// the user who initiated the OAuth flow and login as that user using the access token, used to
// retrieve a client authentication token that is then used to authenticate the user in subsequent
// calls to the API. More info: https://doc.bunq.com/#/psd2
public class BunqOAuthAuthenticator implements OAuth2Authenticator {
    private static final AggregationLogger log =
            new AggregationLogger(BunqOAuthAuthenticator.class);

    private final BunqApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final TemporaryStorage temporaryStorage;
    private final String aggregatorIdentifier;
    private final BunqConfiguration agentConfiguration;

    public BunqOAuthAuthenticator(
            final BunqApiClient apiClient,
            final PersistentStorage persistentStorage,
            final SessionStorage sessionStorage,
            final TemporaryStorage temporaryStorage,
            final String aggregatorIdentifier,
            final BunqConfiguration agentConfiguration) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.temporaryStorage = temporaryStorage;
        this.aggregatorIdentifier = aggregatorIdentifier;
        this.agentConfiguration = agentConfiguration;
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        // If you want to register a new software, the "apiKey" field in development.yml needs to be
        // empty otherwise you'll get an exception.
        String psd2ApiKey = null;
        if (agentConfiguration.getApiKey() != null && !agentConfiguration.getApiKey().isEmpty()) {
            psd2ApiKey = agentConfiguration.getApiKey();
            updateClientAuthToken(BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN);
        } else if (!persistentStorage.containsKey(StorageKeys.PSD2_API_KEY)) {
            registerSoftware();
            psd2ApiKey = persistentStorage.get(BunqConstants.StorageKeys.PSD2_API_KEY);
        }

        // Register a device by using POST v1/device-server using the API key for the secret and
        // passing the installation Token in the X-Bunq-Client-Authentication header.
        apiClient.registerDevice(psd2ApiKey, aggregatorIdentifier);

        // Create your first session by executing POST v1/session-server. Provide the installation
        // Token in the X-Bunq-Client-Authentication header. You will receive a session Token. Use
        // it in any following request in the X-Bunq-Client-Authentication header.
        CreateSessionPSD2ProviderResponse createSessionPSD2ProviderResponse =
                apiClient.createSessionPSD2Provider(
                        persistentStorage.get(BunqConstants.StorageKeys.PSD2_API_KEY));
        persistentStorage.put(
                BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN,
                createSessionPSD2ProviderResponse.getToken());
        updateClientAuthToken(BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN);
        sessionStorage.put(
                BunqConstants.StorageKeys.PSD2_USER_ID,
                createSessionPSD2ProviderResponse.getUserPaymentServiceProvider().getId());

        // We can get the timeout for the OAuthToken from here, not well documented but looking here
        // https://doc.bunq.com/#/session-server/Create_SessionServer we see this line in the
        // description 'A session expires after the same amount of time you have set for Auto Logout
        // in your user account. By default this is 1 week.' So we retrieve this to later be set as
        // the OAuth token expiration time.
        sessionStorage.put(
                BunqConstants.StorageKeys.PSD2_SESSION_TIMEOUT,
                createSessionPSD2ProviderResponse
                        .getUserPaymentServiceProvider()
                        .getSessionTimeout());

        // Call POST /v1/user/{userID}/oauth-client
        AddOAuthClientIdResponse addOAuthClientIdResponse =
                apiClient.addOAuthClientId(
                        sessionStorage.get(BunqConstants.StorageKeys.PSD2_USER_ID));
        sessionStorage.put(
                BunqConstants.StorageKeys.OAUTH_CLIENT_ID,
                addOAuthClientIdResponse.getId().getId());

        // Call GET /v1/user/{userID}/oauth-client/{oauth-clientID}. We will return your Client ID
        // and Client Secret.
        GetClientIdAndSecretResponse getClientIdAndSecretResponse =
                apiClient.getClientIdAndSecret(
                        sessionStorage.get(BunqConstants.StorageKeys.PSD2_USER_ID),
                        sessionStorage.get(BunqConstants.StorageKeys.OAUTH_CLIENT_ID));
        sessionStorage.put(
                BunqConstants.StorageKeys.CLIENT_ID,
                getClientIdAndSecretResponse.getOauthClient().getClientId());
        sessionStorage.put(
                BunqConstants.StorageKeys.CLIENT_SECRET,
                getClientIdAndSecretResponse.getOauthClient().getClientSecret());

        // Call POST /v1/user/{userID}/oauth-client/{oauth-clientID}/callback-url. Include the
        // OAuth
        // callback URL of your application.
        apiClient.registerCallbackUrl(
                sessionStorage.get(BunqConstants.StorageKeys.PSD2_USER_ID),
                sessionStorage.get(BunqConstants.StorageKeys.OAUTH_CLIENT_ID),
                agentConfiguration.getRedirectUrl());

        return BunqConstants.Url.AUTHORIZE
                .queryParam(BunqConstants.QueryParams.RESPONSE_TYPE, BunqConstants.QueryValues.CODE)
                .queryParam(
                        BunqConstants.QueryParams.CLIENT_ID,
                        sessionStorage.get(BunqConstants.StorageKeys.CLIENT_ID))
                .queryParam(
                        BunqConstants.QueryParams.REDIRECT_URI, agentConfiguration.getRedirectUrl())
                .queryParam(BunqConstants.QueryParams.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenExchangeResponse tokenExchangeResponse =
                apiClient.getAccessToken(
                        code,
                        agentConfiguration.getRedirectUrl(),
                        sessionStorage.get(BunqConstants.StorageKeys.CLIENT_ID),
                        sessionStorage.get(BunqConstants.StorageKeys.CLIENT_SECRET));

        if (!"bearer".equals(tokenExchangeResponse.getTokenType())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception();
        }

        return tokenExchangeResponse.toTinkToken(
                sessionStorage
                        .get(BunqConstants.StorageKeys.PSD2_SESSION_TIMEOUT, Integer.class)
                        .orElse(0));
    }

    @Override
    public OAuth2Token refreshAccessToken(String refreshToken) throws BankServiceException {
        return null;
    }

    // Once we have obtained the OAuth access token, we need to use it as a user "API key" to login
    // on behalf of the user with the exact same flow that was used on the pre-PSD2 agent. Read more
    // here: https://doc.bunq.com/#/psd2 and see an example from Bunq here:
    // https://github.com/bunq/psd2_sample_csharp/blob/97ca777894e401ef85e43f9ae0e54a1e501290ac/Program.cs#L135
    @Override
    public void useAccessToken(OAuth2Token accessToken) {
        sessionStorage.put(BunqConstants.StorageKeys.USER_API_KEY, accessToken.getAccessToken());

        // Start by sending the server the public key so they can verify our signature header in
        // future requests
        KeyPair keyPair = RSA.generateKeyPair(2048);
        persistentStorage.put(
                StorageKeys.USER_DEVICE_RSA_SIGNING_KEY_PAIR,
                SerializationUtils.serializeKeyPair(keyPair));
        sessionStorage.remove(BunqBaseConstants.StorageKeys.CLIENT_AUTH_TOKEN);
        InstallResponse installationResponse = apiClient.installation(keyPair.getPublic());

        // This token is used in one of the required headers. This must be set before the next
        // request is done.
        // Persist the client auth token here, cus it will be used in the auto auth
        persistentStorage.put(
                BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN,
                installationResponse.getToken());
        updateClientAuthToken(BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN);

        // This is just to make it obvious that it's a api key we're using
        String userApiKey = sessionStorage.get(BunqBaseConstants.StorageKeys.USER_API_KEY);
        RegisterDeviceResponse registerDeviceResponse =
                apiClient.registerDevice(userApiKey, aggregatorIdentifier);

        persistentStorage.put(
                BunqBaseConstants.StorageKeys.DEVICE_SERVER_ID,
                registerDeviceResponse.getId().getId());

        // Create the session and save session values
        CreateSessionUserAsPSD2ProviderResponse createSessionUserAsPSD2ProviderResponse =
                apiClient.createSessionUserAsPSD2Provider(userApiKey);
        persistentStorage.put(
                BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN,
                createSessionUserAsPSD2ProviderResponse.getToken());
        updateClientAuthToken(BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN);

        sessionStorage.put(
                BunqBaseConstants.StorageKeys.USER_ID,
                createSessionUserAsPSD2ProviderResponse.getUserApiKey().getId());
    }

    // Bunq uses an endpoint to register a PSD2 provider, instead of the usual developer portal used
    // by other banks, this only needs to be run once per QSealC certificate used to obtain the PSD2
    // API key that can then be saved as a secret and used to create future session/register
    // devices.
    private void registerSoftware() {
        KeyPair keyPair = RSA.generateKeyPair(2048);
        persistentStorage.put(
                StorageKeys.PSD2_DEVICE_RSA_SIGNING_KEY_PAIR,
                SerializationUtils.serializeKeyPair(keyPair));

        // Execute POST v1/installation and get your installation Token with a unique random key
        // pair.
        InstallResponse installationResponse = apiClient.installation(keyPair.getPublic());

        // This token is used in one of the required headers. This must be set before the next
        // request is done.
        persistentStorage.put(
                BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN, installationResponse.getToken());
        updateClientAuthToken(BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN);

        // Use the installation Token and your unique PSD2 certificate to call POST
        // v1/payment-service-provider-credential. This will register your software.
        RegisterAsPSD2ProviderResponse registerSoftwareResponse =
                apiClient.registerAsPSD2Provider(
                        keyPair.getPublic(),
                        sessionStorage
                                .get(
                                        BunqBaseConstants.StorageKeys.CLIENT_AUTH_TOKEN,
                                        TokenEntity.class)
                                .orElseThrow(
                                        () ->
                                                new IllegalStateException(
                                                        "No client auth token found.")));

        persistentStorage.put(
                BunqConstants.StorageKeys.PSD2_API_KEY, registerSoftwareResponse.getToken());
    }

    // This is needed due to Bunq's header signing requirement, see https://doc.bunq.com/#/signing
    // depending if we are making calls as a PSD2Provider or on behalf of the user we have to use
    // different keys to sign the "X-Bunq-Client-Signature" header so when we update the client
    // authentication token, we need to update which key should be use to sign the client signature
    // header. Depending on which role we are making the calls as and also on which phase of
    // the authentication flow we are we should use different client authentication tokens,
    // i.e. to register a device and start a session we have to use the token that we got from the
    // installation call while any call done after a session is started should use the token
    // received from in the session response as the client authentication token.
    private void updateClientAuthToken(String storageTokenKey) {
        TokenEntity newClientAuthToken =
                persistentStorage
                        .get(storageTokenKey, TokenEntity.class)
                        .orElseThrow(
                                () -> new IllegalStateException("No client auth token found."));
        sessionStorage.put(BunqConstants.StorageKeys.CLIENT_AUTH_TOKEN, newClientAuthToken);
        String storageDeviceRSASigningKeyPairKey;
        switch (storageTokenKey) {
            case BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN:
                storageDeviceRSASigningKeyPairKey =
                        BunqConstants.StorageKeys.PSD2_DEVICE_RSA_SIGNING_KEY_PAIR;
                break;

            case BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN:
                storageDeviceRSASigningKeyPairKey =
                        BunqBaseConstants.StorageKeys.USER_DEVICE_RSA_SIGNING_KEY_PAIR;
                break;

            default:
                throw new IllegalArgumentException(
                        "Invalid storageToken key : "
                                + storageTokenKey
                                + " should be one of [BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN, BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN]");
        }
        temporaryStorage.put(
                newClientAuthToken.getToken(),
                persistentStorage.get(storageDeviceRSASigningKeyPairKey));
    }
}
