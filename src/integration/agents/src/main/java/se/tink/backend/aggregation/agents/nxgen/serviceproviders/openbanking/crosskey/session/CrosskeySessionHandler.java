package se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.openbanking.crosskey.CrosskeyBaseApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class CrosskeySessionHandler implements SessionHandler {

    private final CrosskeyBaseApiClient apiClient;

    public CrosskeySessionHandler(CrosskeyBaseApiClient apiClient) {

        this.apiClient = apiClient;
    }

    @Override
    public void logout() {}

    @Override
    public void keepAlive() throws SessionException {
        // Keepalive not implemented -- session expires after only 30 seconds
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
