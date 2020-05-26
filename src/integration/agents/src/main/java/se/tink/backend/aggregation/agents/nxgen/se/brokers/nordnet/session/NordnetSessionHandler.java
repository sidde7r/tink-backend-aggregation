package se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.nordnet.NordnetApiClient;
import se.tink.backend.aggregation.nxgen.controllers.authentication.multifactor.thirdpartyapp.oauth2.constants.OAuth2Constants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.core.authentication.OAuth2Token;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

public class NordnetSessionHandler implements SessionHandler {

    private final NordnetApiClient apiClient;
    private final PersistentStorage persistentStorage;

    public NordnetSessionHandler(NordnetApiClient apiClient, PersistentStorage persistentStorage) {
        this.apiClient = apiClient;
        this.persistentStorage = persistentStorage;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        OAuth2Token token =
                persistentStorage
                        .get(OAuth2Constants.PersistentStorageKeys.OAUTH_2_TOKEN, OAuth2Token.class)
                        .filter(t -> !t.hasAccessExpired())
                        .orElseThrow(SessionError.SESSION_EXPIRED::exception);

        if (!apiClient.authorizeSession(token)) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
