package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator;

import java.security.KeyPair;
import java.util.Objects;
import se.tink.backend.agents.rpc.Credentials;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.AuthenticationException;
import se.tink.backend.aggregation.agents.exceptions.AuthorizationException;
import se.tink.backend.aggregation.agents.exceptions.errors.LoginError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionUserResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.InstallResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.authenticator.rpc.RegisterDeviceResponse;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bunq.entities.ErrorResponse;
import se.tink.backend.aggregation.agents.utils.crypto.RSA;
import se.tink.backend.aggregation.nxgen.controllers.authentication.Authenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;
import se.tink.libraries.serialization.utils.SerializationUtils;

// Refer to https://doc.bunq.com/ for BUNQ API DOC
public class BunqRegistrationAuthenticator implements Authenticator {
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;
    private final TemporaryStorage temporaryStorage;
    private final BunqApiClient apiClient;
    private final String aggregatorIdentifier;

    public BunqRegistrationAuthenticator(
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TemporaryStorage temporaryStorage,
            BunqApiClient apiClient,
            String aggregatorIdentifier) {
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
        this.temporaryStorage = temporaryStorage;
        this.apiClient = apiClient;
        this.aggregatorIdentifier = aggregatorIdentifier;
    }

    @Override
    public void authenticate(Credentials credentials)
            throws AuthenticationException, AuthorizationException {
        KeyPair keyPair = RSA.generateKeyPair(2048);
        persistentStorage.put(
                BunqBaseConstants.StorageKeys.USER_DEVICE_RSA_SIGNING_KEY_PAIR,
                SerializationUtils.serializeKeyPair(keyPair));

        try {
            // Start by sending the server the public key so they can verify our signature header in
            // future requests
            InstallResponse installationResponse = apiClient.installation(keyPair.getPublic());

            // This token is used in one of the required headers. This must be set before the next
            // request is done.
            // Persist the client auth token here, cus it will be used in the auto auth
            persistentStorage.put(
                    BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN,
                    installationResponse.getToken());
            BunqAuthenticator.updateClientAuthToken(
                    sessionStorage, persistentStorage, temporaryStorage);

            // This is just to make it obvious that it's a api key we're using
            String apiKey = credentials.getField(Field.Key.PASSWORD);
            RegisterDeviceResponse registerDeviceResponse =
                    apiClient.registerDevice(apiKey, aggregatorIdentifier);

            // Persist everything we need need to save
            persistentStorage.put(
                    BunqBaseConstants.StorageKeys.DEVICE_SERVER_ID,
                    registerDeviceResponse.getId().getId());

            // Create the session and save session values
            CreateSessionUserResponse createSessionUserResponse =
                    apiClient.createSessionUser(apiKey);
            persistentStorage.put(
                    BunqBaseConstants.StorageKeys.USER_CLIENT_AUTH_TOKEN,
                    createSessionUserResponse.getToken());
            BunqAuthenticator.updateClientAuthToken(
                    sessionStorage, persistentStorage, temporaryStorage);
            sessionStorage.put(
                    BunqBaseConstants.StorageKeys.USER_ID,
                    createSessionUserResponse.getUserPerson().getId());
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);

            String errorDescription =
                    errorResponse
                            .getErrorDescription()
                            .orElseThrow(
                                    () ->
                                            new IllegalStateException(
                                                    "Could not authenticate user", e));

            if (!Objects.equals(
                    "user credentials are incorrect. incorrect api key or ip address.",
                    errorDescription.toLowerCase())) {
                throw new IllegalStateException("Could not authenticate user", e);
            }

            throw LoginError.INCORRECT_CREDENTIALS.exception();
        }
    }
}
