package se.tink.backend.aggregation.agents.nxgen.de.banks.fints.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.de.banks.fints.FinTsApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class FinTsSessionHandler implements SessionHandler {

    private FinTsApiClient apiClient;

    public FinTsSessionHandler(FinTsApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.end();
    }

    @Override
    public void keepAlive() throws SessionException {

        if (!apiClient.keepAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
