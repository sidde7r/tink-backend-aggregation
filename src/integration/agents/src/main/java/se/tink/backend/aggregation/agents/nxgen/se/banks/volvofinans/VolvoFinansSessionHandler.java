package se.tink.backend.aggregation.agents.nxgen.se.banks.volvofinans;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class VolvoFinansSessionHandler implements SessionHandler {

    private final VolvoFinansApiClient apiClient;

    protected VolvoFinansSessionHandler(VolvoFinansApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.keepAlive();
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
