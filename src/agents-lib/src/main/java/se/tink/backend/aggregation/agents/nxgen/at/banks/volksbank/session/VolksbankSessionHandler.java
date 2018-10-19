package se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.at.banks.volksbank.VolksbankApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class VolksbankSessionHandler implements SessionHandler {

    private final VolksbankApiClient apiClient;

    public VolksbankSessionHandler(VolksbankApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            apiClient.getMain();
        } catch (Exception e) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
