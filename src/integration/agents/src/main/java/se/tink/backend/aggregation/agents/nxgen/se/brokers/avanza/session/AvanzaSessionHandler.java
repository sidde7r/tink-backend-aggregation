package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.session;

import java.util.List;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AuthSessionStorageHelper;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class AvanzaSessionHandler implements SessionHandler {
    private final AvanzaApiClient apiClient;
    private final AuthSessionStorageHelper authSessionStorage;

    public AvanzaSessionHandler(
            AvanzaApiClient apiClient, AuthSessionStorageHelper authSessionStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
    }

    @Override
    public void logout() {
        // FIXME: logging out from the wrong session fails with 401
        List<String> authSessions = authSessionStorage.getAuthSessions();
        if (authSessions.size() == 1) {
            final String authSession = authSessions.iterator().next();
            apiClient.logout(authSession);
        }
    }

    @Override
    public void keepAlive() throws SessionException {
        List<String> authSessions = authSessionStorage.getAuthSessions();
        if (authSessions.isEmpty()) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
        try {
            final String firstSession = authSessions.iterator().next();

            apiClient.fetchAccounts(firstSession);
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED, e);
        }
    }
}
