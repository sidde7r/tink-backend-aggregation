package se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.es.banks.lacaixa.LaCaixaApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class LaCaixaSessionHandler implements SessionHandler {

    private LaCaixaApiClient apiClient;

    public LaCaixaSessionHandler(LaCaixaApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
