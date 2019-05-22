package se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator;

import com.google.api.client.repackaged.com.google.common.base.Strings;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.BunqApiClient;
import se.tink.backend.aggregation.agents.nxgen.nl.banks.bunq.authenticator.rpc.CreateSessionUserResponse;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.BunqBaseConstants;
import se.tink.backend.aggregation.agents.nxgen.nl.common.bunq.entities.ErrorResponse;
import se.tink.backend.aggregation.log.AggregationLogger;
import se.tink.backend.aggregation.nxgen.controllers.authentication.automatic.AutoAuthenticator;
import se.tink.backend.aggregation.nxgen.http.HttpResponse;
import se.tink.backend.aggregation.nxgen.http.exceptions.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;
import se.tink.backend.aggregation.nxgen.storage.TemporaryStorage;

public class BunqAutoAuthenticator implements AutoAuthenticator {
    private static final AggregationLogger log = new AggregationLogger(BunqAutoAuthenticator.class);
    private final SessionStorage sessionStorage;
    private final PersistentStorage persistentStorage;
    private final TemporaryStorage temporaryStorage;
    private final BunqApiClient apiClient;

    public BunqAutoAuthenticator(
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage,
            TemporaryStorage temporaryStorage,
            BunqApiClient apiClient) {
        this.sessionStorage = sessionStorage;
        this.persistentStorage = persistentStorage;
        this.temporaryStorage = temporaryStorage;
        this.apiClient = apiClient;
    }

    @Override
    public void autoAuthenticate() throws SessionException {
        validateRsaKeyPairUsedLaterInFilter();
        try {
            // Here we need to use the token got from installation
            BunqAuthenticator.updateClientAuthToken(
                    sessionStorage, persistentStorage, temporaryStorage);
            CreateSessionUserResponse createSessionUserResponse =
                    apiClient.createSessionUser(
                            sessionStorage.get(BunqBaseConstants.StorageKeys.USER_API_KEY));
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
            log.warnExtraLong(
                    errorResponse.getErrorDescription().orElse("Error description was null"),
                    BunqBaseConstants.LogTags.AUTO_AUTHENTICATION_FAILED,
                    e);

            throw SessionError.SESSION_EXPIRED.exception();
        }
    }

    private void validateRsaKeyPairUsedLaterInFilter() {
        if (Strings.isNullOrEmpty(
                persistentStorage.get(
                        BunqBaseConstants.StorageKeys.USER_DEVICE_RSA_SIGNING_KEY_PAIR))) {
            String errorMessage =
                    String.format(
                            "PersistentStorage is missing %s",
                            BunqBaseConstants.StorageKeys.USER_DEVICE_RSA_SIGNING_KEY_PAIR);
            log.warnExtraLong(errorMessage, BunqBaseConstants.LogTags.AUTO_AUTHENTICATION_FAILED);
            throw new IllegalStateException(errorMessage);
        }
    }
}
