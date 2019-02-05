package se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.sessionhandler;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.be.banks.belfius.BelfiusApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BelfiusSessionHandler implements SessionHandler {

    private final BelfiusApiClient apiClient;

    public BelfiusSessionHandler(BelfiusApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.keepAlive();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
