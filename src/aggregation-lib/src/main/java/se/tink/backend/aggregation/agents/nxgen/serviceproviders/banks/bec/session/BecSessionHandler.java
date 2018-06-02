package se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.serviceproviders.banks.bec.BecApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;

public class BecSessionHandler implements SessionHandler {

    private final BecApiClient apiClient;

    public BecSessionHandler(BecApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {
        apiClient.logout();
    }

    @Override
    public void keepAlive() throws SessionException {
        throw SessionError.SESSION_EXPIRED.exception();
    }
}
