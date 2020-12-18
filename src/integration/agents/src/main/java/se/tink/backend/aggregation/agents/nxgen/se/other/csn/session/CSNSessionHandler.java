package se.tink.backend.aggregation.agents.nxgen.se.other.csn.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNApiClient;
import se.tink.backend.aggregation.agents.nxgen.se.other.csn.CSNConstants;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CSNSessionHandler implements SessionHandler {
    private final CSNApiClient apiClient;
    private final SessionStorage sessionStorage;

    public CSNSessionHandler(CSNApiClient apiClient, SessionStorage sessionStorage) {
        this.apiClient = apiClient;
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!sessionStorage.containsKey(CSNConstants.Storage.ACCESS_TOKEN)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
