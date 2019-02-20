package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator;

import org.assertj.core.util.Strings;
import se.tink.backend.agents.rpc.Field;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.error.ErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.agents.rpc.Credentials;

public class BunqAutoAuthenticator implements AutoAuthenticator {
    private static final AggregationLogger log = new AggregationLogger(BunqAutoAuthenticator.class);
    private final Credentials credentials;
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final BunqApiClient apiClient;

    public BunqAutoAuthenticator(Credentials credentials, PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            BunqApiClient apiClient) {
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.apiClient = apiClient;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        validateRsaKeyPairUsedLaterInFilter();
        try {
            // Here we need to use the token got from installation
            sessionStorage.put(BunqConstants.StorageKeys.CLIENT_AUTH_TOKEN,
                    persistentStorage.get(BunqConstants.StorageKeys.CLIENT_AUTH_TOKEN));
            String apiKey = credentials.getField(Field.Key.PASSWORD);
            CreateSessionResponse createSessionResponse = apiClient.createSession(apiKey);
            sessionStorage.put(BunqConstants.StorageKeys.CLIENT_AUTH_TOKEN, createSessionResponse.getToken());
            sessionStorage.put(BunqConstants.StorageKeys.USER_ID, createSessionResponse.getUserPerson().getId());
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            log.warnExtraLong(errorResponse.getErrorDescription().orElse("Error description was null"),
                    BunqConstants.LogTags.AUTO_AUTHENTICATION_FAILED, e);

            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private void validateRsaKeyPairUsedLaterInFilter() {
        if (Strings.isNullOrEmpty(
                persistentStorage.get(BunqConstants.StorageKeys.DEVICE_RSA_SIGNING_KEY_PAIR))) {
            String errorMessage =
                    String.format(
                            "PersistentStorage is missing %s",
                            BunqConstants.StorageKeys.DEVICE_RSA_SIGNING_KEY_PAIR);
            log.warnExtraLong(errorMessage, BunqConstants.LogTags.AUTO_AUTHENTICATION_FAILED);
            throw new IllegalStateException(errorMessage);
        }
    }
}
