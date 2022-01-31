package se.tink.backend.aggregation.agents.nxgen.demo.banks.dk;

import lombok.RequiredArgsConstructor;
import se.tink.backend.aggregation.agents.exceptions.SessionException;
import se.tink.backend.aggregation.agents.exceptions.errors.SessionError;
import se.tink.backend.aggregation.nxgen.controllers.session.SessionHandler;
import se.tink.backend.aggregation.nxgen.storage.PersistentStorage;

@RequiredArgsConstructor
public class DkDemoSessionHandler implements SessionHandler {

    private final PersistentStorage persistentStorage;

    @Override
    public void logout() {
        // ignore
    }

    @Override
    public void keepAlive() throws SessionException {
        if (!persistentStorage.containsKey(DkDemoConstants.IS_AUTHENTICATED_STORAGE_KEY)) {
            throw SessionError.SESSION_EXPIRED.exception();
        }
    }
}
