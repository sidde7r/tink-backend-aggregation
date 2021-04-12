package se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.ruralvia.RuralviaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class RuralviaSessionHandler implements SessionHandler {

    private final RuralviaApiClient apiClient;

    public RuralviaSessionHandler(RuralviaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.keepAlive()) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
