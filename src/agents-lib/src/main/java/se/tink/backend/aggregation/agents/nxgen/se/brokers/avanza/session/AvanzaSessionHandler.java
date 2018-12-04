package se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.session;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.brokers.avanza.AvanzaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class AvanzaSessionHandler implements SessionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(AvanzaSessionHandler.class);

    private final AvanzaApiClient apiClient;
    private final SessionStorage sessionStorage;

    public AvanzaSessionHandler(AvanzaApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        sessionStorage.keySet().forEach(apiClient::logout);
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            sessionStorage.keySet().forEach(apiClient::fetchAccounts);
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
