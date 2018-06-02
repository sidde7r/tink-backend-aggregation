package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator;

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
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.rpc.Credentials;
import se.tink.backend.aggregation.rpc.Field;

public class BunqAuthenticationAuthenticator implements AutoAuthenticator {
    private static final AggregationLogger log = new AggregationLogger(BunqAuthenticationAuthenticator.class);
    private final Credentials credentials;
    private final SessionStorage sessionStorage;
    private final BunqApiClient apiClient;

    public BunqAuthenticationAuthenticator(Credentials credentials, SessionStorage sessionStorage,
            BunqApiClient apiClient) {
        this.credentials = credentials;
        this.sessionStorage = sessionStorage;
        this.apiClient = apiClient;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        try {
            String apiKey = credentials.getField(Field.Key.PASSWORD);
            CreateSessionResponse createSessionResponse = apiClient.createSession(apiKey);
            sessionStorage.put(BunqConstants.StorageKeys.SESSION_TOKEN, createSessionResponse.getToken());
            sessionStorage.put(BunqConstants.StorageKeys.USER_ID, createSessionResponse.getUserPerson().getId());
        } catch (HttpResponseException e) {
            HttpResponse response = e.getResponse();
            ErrorResponse errorResponse = response.getBody(ErrorResponse.class);
            log.warnExtraLong(errorResponse.getErrorDescription().orElse("Error description was null"),
                    BunqConstants.LogTags.AUTO_AUTHENTICATION_FAILED, e);

            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
