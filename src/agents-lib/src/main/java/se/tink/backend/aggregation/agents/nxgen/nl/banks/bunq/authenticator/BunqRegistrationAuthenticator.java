package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator;

import java.security.KeyPair;
import java.util.Objects;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.error.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.libraries.serialization.utils.SerializationUtils;

// Refer to https://doc.bunq.com/ for BUNQ API DOC
public class BunqRegistrationAuthenticator implements Authenticator {
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final BunqApiClient apiClient;
    private final String aggregatorIdentifier;

    public BunqRegistrationAuthenticator(PersistentStorage persistentStorage, SessionStorage sessionStorage,
            BunqApiClient apiClient, String aggregatorIdentifier) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    @Override
    public void authenticate(Credentials credentials) throws AuthenticationException, AuthorizationException {
        KeyPair keyPair = RSA.generateKeyPair(2048);
        persistentStorage.put(BunqConstants.StorageKeys.DEVICE_RSA_SIGNING_KEY_PAIR, SerializationUtils.serializeKeyPair(keyPair));

        try {
            // Start by sending the server the public key so they can verify our signature header in future requests
            InstallResponse installationResponse = apiClient.installation(keyPair.getPublic());

            // This token is used in one of the required headers. This must be set before the next request is done.
            // Persist the client auth token here, cus it will be used in the auto auth
            persistentStorage.put(BunqConstants.StorageKeys.CLIENT_AUTH_TOKEN, installationResponse.getToken());
            sessionStorage.put(BunqConstants.StorageKeys.CLIENT_AUTH_TOKEN, installationResponse.getToken());

            // This is just to make it obvious that it's a api key we're using
            String apiKey = credentials.getField(Field.Key.PASSWORD);
            RegisterDeviceResponse registerDeviceResponse = apiClient.registerDevice(apiKey, aggregatorIdentifier);

            // Persist everything we need need to save
            persistentStorage.put(BunqConstants.StorageKeys.BUNQ_PUBLIC_KEY, installationResponse.getServerPublicKey());
            persistentStorage.put(BunqConstants.StorageKeys.DEVICE_SERVER_ID, registerDeviceResponse.getId());

            // Create the session and save session values
            CreateSessionResponse createSessionResponse = apiClient.createSession(apiKey);
            sessionStorage.put(BunqConstants.StorageKeys.CLIENT_AUTH_TOKEN, createSessionResponse.getToken());
            sessionStorage.put(BunqConstants.StorageKeys.USER_ID, createSessionResponse.getUserPerson().getId());
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            String errorDescription = errorResponse.getErrorDescription().orElseThrow(() ->
                    new IllegalStateException("Could not authenticate user", e));

            if (!Objects.equals("user credentials are incorrect. incorrect api key or ip address.",
                    errorDescription.toLowerCase())) {
                throw new IllegalStateException("Could not authenticate user", e);
            }

            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
