package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.session;

import org.apache.http.HttpStatus;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetConstants;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.http.response.HttpResponseException;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class NordnetSessionHandler implements SessionHandler {

    private final NordnetApiClient apiClient;
    private final PersistentStorage persistentStorage;
    private final SessionStorage sessionStorage;

    public NordnetSessionHandler(
            NordnetApiClient apiClient,
            PersistentStorage persistentStorage,
            SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        try {
            if (apiClient.isPasswordLogin()) {
                String sessionKey =
                        sessionStorage
                                .get(NordnetConstants.StorageKeys.SESSION_KEY, String.class)
                                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
                apiClient.authorizeSession(sessionKey);

            } else {
                OAuth2Token token =
                        persistentStorage
                                .get(
                                        OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN,
                                        OAuth2Token.class)
                                .filter(t -> !t.hasAccessExpired())
                                .orElseThrow(SessionError.SESSION_EXPIRED::exception);
                apiClient.authorizeSession(token);
            }
        } catch (HttpResponseException e) {
            if (e.getResponse().getStatus() == HttpStatus.SC_UNAUTHORIZED
                    && e.getResponse()
                            .getBody(String.class)
                            .contains(NordnetConstants.Errors.INVALID_SESSION)) {
                throw SessionError.SESSION_EXPIRED.exception();
            }
            // Re-throw unknown exception
            throw e;
        }
    }
}
