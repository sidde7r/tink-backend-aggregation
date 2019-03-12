package se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.no.openbanking.sparebank1.SpareBank1ApiClient;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.exceptions.NotImplementedException;

public final class SpareBank1SessionHandler implements SessionHandler {
    private final SpareBank1ApiClient apiClient;

    public SpareBank1SessionHandler(SpareBank1ApiClient apiClient) {
        this.apiClient = apiClient;
    }

    @Override
    public void logout() {

    }

    @Override
    public void keepAlive() throws SessionException {
        try {
            throw new NotImplementedException("keepAlive not implemented");
        } catch (Exception e) {
            throw new SessionException(SessionError.SESSION_EXPIRED);
        }
    }
}
