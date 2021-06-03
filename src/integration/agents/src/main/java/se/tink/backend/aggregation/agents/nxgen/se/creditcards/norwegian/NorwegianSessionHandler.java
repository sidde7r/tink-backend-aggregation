package se.tink.backend.aggregation.agents.nxgen.se.creditcards.norwegian;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class NorwegianSessionHandler implements SessionHandler {
    private final NorwegianApiClient apiClient;

    public NorwegianSessionHandler(NorwegianApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        // NOOP
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.isLoggedIn()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
