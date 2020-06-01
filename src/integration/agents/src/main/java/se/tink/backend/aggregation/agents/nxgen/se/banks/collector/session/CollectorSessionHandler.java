package se.tink.backend.aggregation.agents.nxgen.se.banks.collector.session;

import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.agents.nxgen.se.banks.collector.CollectorConstants.Storage;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.SessionStorage;

public class CollectorSessionHandler implements SessionHandler {
    private final SessionStorage sessionStorage;

    public CollectorSessionHandler(SessionStorage sessionStorage) {
        this.sessionStorage = sessionStorage;
    }

    @Override
    public void logout() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!sessionStorage.containsKey(Storage.BEARER_TOKEN)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
