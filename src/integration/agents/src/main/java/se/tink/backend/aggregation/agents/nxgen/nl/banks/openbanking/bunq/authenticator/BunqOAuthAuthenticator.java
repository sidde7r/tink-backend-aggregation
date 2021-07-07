package se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator;

import java.lang.invoke.MethodHandles;
import java.security.KeyPair;
import org.assertj.core.util.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceError;
import se.tink.backend.aggregation.agents.exceptions.bankservice.BankServiceException;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqClientAuthTokenHandler;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants.StorageKeys;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.BunqConstants.Urls;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.CreateSessionUserAsPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.authenticator.rpc.TokenExchangeResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.openbanking.bunq.configuration.BunqConfiguration;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.CreateSessionPSD2ProviderResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.configuration.agents.AgentConfiguration;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.OAuth2Authenticator;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.http.url.URL;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

// The OAuth flow for Bunq is different than the rest, they use the access token as an "API key" for
// the user who initiated the OAuth flow and login as that user using the access token, used to
// retrieve a client authentication token that is then used to authenticate the user in subsequent
// calls to the API. More info: https://doc.bunq.com/#/psd2
public class BunqOAuthAuthenticator implements OAuth2Authenticator {
    private static final Logger logger =
            LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    private final BunqApiClient apiClient;
    private final BunqClientAuthTokenHandler clientAuthTokenHandler;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final String aggregatorIdentifier;
    private final BunqConfiguration agentConfiguration;
    private final String redirectUrl;

    public BunqOAuthAuthenticator(
            final BunqApiClient apiClient,
            final BunqClientAuthTokenHandler clientAuthTokenHandler,
            final PersistentStorage persistentStorage,
            final SessionStorage sessionStorage,
            final String aggregatorIdentifier,
            final AgentConfiguration<BunqConfiguration> agentConfiguration) {
        this.apiClient = apiClient;
        this.clientAuthTokenHandler = clientAuthTokenHandler;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.aggregatorIdentifier = aggregatorIdentifier;
        this.agentConfiguration = agentConfiguration.getProviderSpecificConfiguration();
        this.redirectUrl = agentConfiguration.getRedirectUrl();
    }

    @Override
    public URL buildAuthorizeUrl(String state) {
        // Only for the case where this is the first session we create after registration, we need
        // to store the client auth token from the registration process as well as add the correct
        // key to sign the create session request's header
        if (!persistentStorage.containsKey(BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN)
                || !sessionStorage.containsKey(BunqBaseConstants.StorageKeys.CLIENT_AUTH_TOKEN)) {
            persistentStorage.put(
                    StorageKeys.PSD2_DEVICE_RSA_SIGNING_KEY_PAIR,
                    agentConfiguration.getPsd2InstallationKeyPair());
            persistentStorage.put(
                    BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN,
                    agentConfiguration.getPsd2ClientAuthToken());
            clientAuthTokenHandler.updateClientAuthToken(
                    BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN);
        }

        // Create your session by executing POST v1/session-server. Provide the installation
        // Token in the X-Bunq-Client-Authentication header. You will receive a session Token. Use
        // it in any following request in the X-Bunq-Client-Authentication header.
        CreateSessionPSD2ProviderResponse createSessionPSD2ProviderResponse =
                apiClient.createSessionPSD2Provider(agentConfiguration.getPsd2ApiKey());
        persistentStorage.put(
                BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN,
                createSessionPSD2ProviderResponse.getToken());
        clientAuthTokenHandler.updateClientAuthToken(
                BunqConstants.StorageKeys.PSD2_CLIENT_AUTH_TOKEN);

        // We can get the timeout for the OAuthToken from here, not well documented but looking here
        // https://doc.bunq.com/#/session-server/Create_SessionServer we see this line in the
        // description 'A session expires after the same amount of time you have set for Auto Logout
        // in your user account. By default this is 1 week.' So we retrieve this to later be set as
        // the OAuth token expiration time.
        // More info about which timeout should be used in this thread
        // https://together.bunq.com/d/3581-relationship-between-autologout-time-and-token-from-session-for-oauth2-flow
        // Specifically: "The session time out is based on the requested by user session time out."
        // and we are the requesting user.
        sessionStorage.put(
                BunqConstants.StorageKeys.PSD2_SESSION_TIMEOUT,
                createSessionPSD2ProviderResponse
                        .getUserPaymentServiceProvider()
                        .getSessionTimeout());

        return Urls.AUTHORIZE
                .queryParam(BunqConstants.QueryParams.RESPONSE_TYPE, BunqConstants.QueryValues.CODE)
                .queryParam(BunqConstants.QueryParams.CLIENT_ID, agentConfiguration.getClientId())
                .queryParam(BunqConstants.QueryParams.REDIRECT_URI, redirectUrl)
                .queryParam(BunqConstants.QueryParams.STATE, state);
    }

    @Override
    public OAuth2Token exchangeAuthorizationCode(String code) throws BankServiceException {
        TokenExchangeResponse tokenExchangeResponse =
                apiClient.getAccessToken(
                        code,
                        redirectUrl,
                        agentConfiguration.getClientId(),
                        agentConfiguration.getClientSecret());

        if (!"bearer".equals(tokenExchangeResponse.getTokenType())) {
            throw BankServiceError.BANK_SIDE_FAILURE.exception(
                    "Token is not of bearer type"
                            + (Strings.isNullOrEmpty(tokenExchangeResponse.getTokenType())
                                    ? "."
                                    : ": " + tokenExchangeResponse.getTokenType()));
        }

        return tokenExchangeResponse.toTinkToken();
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
        // We don't need to go through the use authentication flow if there is already an open
        // session for the user
        if (!isActiveSession()) {
            sessionStorage.put(
                    BunqBaseConstants.StorageKeys.USER_API_KEY, accessToken.getAccessToken());

            // Start by sending the server the public key so they can verify our signature header in
            // future requests
            KeyPair keyPair = RSA.generateKeyPair(2048);
            persistentStorage.put(
                    BunqBaseConstants.StorageKeys.USER_DEVICE_RSA_SIGNING_KEY_PAIR,
                    SerializationUtils.serializeKeyPair(keyPair));
            sessionStorage.remove(BunqBaseConstants.StorageKeys.CLIENT_AUTH_TOKEN);
            InstallResponse installationResponse = apiClient.installation(keyPair.getPublic());

            // This token is used in one of the required headers. This must be set before the next
            // request is done.
            // Persist the client auth token here
            persistentStorage.put(
                    BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN,
                    installationResponse.getToken());
            clientAuthTokenHandler.updateClientAuthToken(
                    BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN);

            // This is just to make it obvious that it's a api key we're using
            String userApiKey = sessionStorage.get(BunqBaseConstants.StorageKeys.USER_API_KEY);
            apiClient.registerDevice(userApiKey, aggregatorIdentifier);

            // Create the session and save session values
            CreateSessionUserAsPSD2ProviderResponse createSessionUserAsPSD2ProviderResponse =
                    apiClient.createSessionUserAsPSD2Provider(userApiKey);
            persistentStorage.put(
                    BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN,
                    createSessionUserAsPSD2ProviderResponse.getToken());
            clientAuthTokenHandler.updateClientAuthToken(
                    BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN);

            persistentStorage.put(
                    BunqBaseConstants.StorageKeys.USER_ID,
                    createSessionUserAsPSD2ProviderResponse.getUserApiKey().getId());
        }
        sessionStorage.put(
                BunqBaseConstants.StorageKeys.USER_ID,
                persistentStorage.get(BunqBaseConstants.StorageKeys.USER_ID));
    }

    private boolean isActiveSession() {
        if (!persistentStorage.containsKey(BunqBaseConstants.StorageKeys.USER_ID)) {
            return false;
        }

        try {
            // Need to update the client authentication token before we can make the list accounts
            // call
            clientAuthTokenHandler.updateClientAuthToken(
                    BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN);
            apiClient.listAccounts(persistentStorage.get(BunqBaseConstants.StorageKeys.USER_ID));
            return true;
        } catch (HttpResponseException e) {
            logger.info(
                    "Session was not active for userId : "
                            + persistentStorage.get(BunqBaseConstants.StorageKeys.USER_ID),
                    e);
            return false;
        }
    }
}
