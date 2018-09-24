package se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.fr.banks.lcl.LclApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class LclSessionHandler implements SessionHandler {

    private final LclApiClient apiClient;

    public LclSessionHandler(LclApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        //
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!apiClient.isAlive()) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
