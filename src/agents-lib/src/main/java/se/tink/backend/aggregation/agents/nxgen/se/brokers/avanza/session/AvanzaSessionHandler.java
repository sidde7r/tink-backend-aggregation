package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaAuthSessionStorage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class AvanzaSessionHandler implements SessionHandler {
    private final AvanzaApiClient apiClient;
    private final AvanzaAuthSessionStorage authSessionStorage;

    public AvanzaSessionHandler(
            AvanzaApiClient apiClient, AvanzaAuthSessionStorage authSessionStorage) {
        this.apiClient = apiClient;
        this.authSessionStorage = authSessionStorage;
    }

    @Override
    public void logout() {
        authSessionStorage.keySet().forEach(apiClient::logout);
    }

    @Override
    public void keepAlive() throws SessionException {
        if (authSessionStorage.isEmpty()) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
        try {
            final String firstSession = authSessionStorage.keySet().iterator().next();

            apiClient.fetchAccounts(firstSession);
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
